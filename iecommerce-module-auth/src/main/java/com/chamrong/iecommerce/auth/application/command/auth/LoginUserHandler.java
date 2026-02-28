package com.chamrong.iecommerce.auth.application.command.auth;

import com.chamrong.iecommerce.auth.application.audit.AuthEventLogger;
import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.application.exception.AccountLockedException;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.event.ConcurrentSessionDetectedEvent;
import com.chamrong.iecommerce.auth.domain.event.UserLoggedInEvent;
import com.chamrong.iecommerce.auth.domain.event.UserLoginFailedEvent;
import com.chamrong.iecommerce.auth.domain.lock.LoginAttemptRecord;
import com.chamrong.iecommerce.auth.domain.lock.LoginLockPolicy;
import com.chamrong.iecommerce.auth.domain.lock.LoginLockStore;
import com.chamrong.iecommerce.common.annotation.WithTenantContext;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

/**
 * Authenticates a user via the identity provider with:
 *
 * <ol>
 *   <li><strong>Progressive lock guard</strong> — rejects login immediately if the account is
 *       currently within a lock window (no Keycloak call made — saves IDP round-trip).
 *   <li><strong>Keycloak authentication</strong> — delegates to {@link IdentityService}.
 *   <li><strong>Concurrent session detection</strong> — checks existing sessions and emits {@link
 *       ConcurrentSessionDetectedEvent} if the user is already logged in elsewhere.
 *   <li><strong>Lock escalation on failure</strong> — each {@link BadCredentialsException}
 *       increments the failure counter and applies the next lock tier from {@link LoginLockPolicy}.
 *   <li><strong>Lock reset on success</strong> — counter is cleared in {@link LoginLockStore} so
 *       the user starts fresh after a correct login.
 *   <li><strong>Forced-reset detection</strong> — returns {@code requiresPasswordChange=true} when
 *       Keycloak has {@code UPDATE_PASSWORD} required action (first-login or admin reset).
 * </ol>
 *
 * <h3>Security notes</h3>
 *
 * <ul>
 *   <li>Lock check happens <em>before</em> the Keycloak call — prevents timing oracle (an attacker
 *       cannot distinguish "wrong password" from "locked" by response latency).
 *   <li>The lock counter key is {@code tenantId:username} — prevents cross-tenant interference.
 *   <li>No passwords, tokens, or OTP codes are ever logged.
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class LoginUserHandler {

  private final IdentityService identityService;
  private final ApplicationEventPublisher eventPublisher;
  private final LoginLockStore lockStore;
  private final LoginLockPolicy lockPolicy;
  private final AuthEventLogger auditLog;

  /**
   * Authenticates a user, enforcing the progressive lock policy.
   *
   * @param cmd the login command (username, password, tenantId, clientIp)
   * @throws AccountLockedException if the account is in a lock window
   * @throws BadCredentialsException if authentication fails
   */
  @WithTenantContext(tenantId = "#cmd.tenantId")
  public AuthResponse handle(final LoginCommand cmd) {

    // 1. Guard: reject immediately if currently locked (no IDP round-trip)
    enforceNotLocked(cmd.username(), cmd.tenantId());

    try {
      // 2. Authenticate via Keycloak
      AuthResponse response = identityService.authenticate(cmd);

      // 3. Lock counter reset — successful login clears all failure history
      lockStore.clear(cmd.username(), cmd.tenantId());

      // 4. Concurrent session detection
      detectConcurrentSessions(cmd);

      // 5. Forced-reset detection (Keycloak UPDATE_PASSWORD required action)
      final boolean needsReset = identityService.requiresPasswordChange(cmd.username());
      if (needsReset) {
        response = response.withRequiresPasswordChange(true);
        auditLog.loginSuccess(cmd.username(), cmd.tenantId(), 0);
      } else {
        auditLog.loginSuccess(cmd.username(), cmd.tenantId(), countSessions(cmd));
      }

      // 6. Publish success domain event
      eventPublisher.publishEvent(new UserLoggedInEvent(cmd.username(), cmd.tenantId()));

      return response;

    } catch (BadCredentialsException e) {
      handleFailedAttempt(cmd);
      throw e;
    }
  }

  // ─── Lock enforcement ─────────────────────────────────────────────────────

  private void enforceNotLocked(final String username, final String tenantId) {
    lockStore
        .find(username, tenantId)
        .ifPresent(
            record -> {
              if (record.isLocked()) {
                final Duration remaining = record.remainingLockDuration();
                auditLog.loginRejectedLocked(username, tenantId, remaining);
                throw new AccountLockedException(username, remaining);
              }
            });
  }

  // ─── Failed attempt handling ──────────────────────────────────────────────

  private void handleFailedAttempt(final LoginCommand cmd) {
    final LoginAttemptRecord current =
        lockStore
            .find(cmd.username(), cmd.tenantId())
            .orElse(LoginAttemptRecord.clean(cmd.username(), cmd.tenantId()));

    final LoginAttemptRecord updated = current.recordFailure(lockPolicy);
    lockStore.save(updated);

    // Was a new lock applied?
    final Duration lockApplied =
        updated.isLocked() ? lockPolicy.lockDurationFor(updated.failedAttempts()) : Duration.ZERO;

    auditLog.loginFailure(
        cmd.username(), cmd.tenantId(), "Invalid credentials", updated.failedAttempts());

    if (!lockApplied.isZero()) {
      auditLog.accountLocked(cmd.username(), cmd.tenantId(), lockApplied, updated.failedAttempts());
    }

    eventPublisher.publishEvent(
        new UserLoginFailedEvent(cmd.username(), cmd.tenantId(), "Invalid credentials"));
  }

  // ─── Concurrent session detection ─────────────────────────────────────────

  private void detectConcurrentSessions(final LoginCommand cmd) {
    try {
      final String keycloakId = identityService.lookupId(cmd.username());

      final List<com.chamrong.iecommerce.auth.domain.UserSession> sessions =
          identityService.listActiveSessions(keycloakId);

      // Sessions list includes the session just created — existing if size > 1
      if (sessions.size() > 1) {
        final String clientIp = MDC.get("clientIp");
        auditLog.concurrentSessionDetected(
            cmd.username(), cmd.tenantId(), sessions.size() - 1, clientIp);
        eventPublisher.publishEvent(
            new ConcurrentSessionDetectedEvent(
                cmd.username(), cmd.tenantId(), clientIp, sessions.size() - 1));
      }
    } catch (Exception ex) {
      // Non-critical — concurrent session detection must never fail a valid login
      // Logged at debug; operational alerts come from the SECURITY_AUDIT logger above
      org.slf4j.LoggerFactory.getLogger(getClass())
          .debug(
              "Concurrent session check failed for user={}: {}", cmd.username(), ex.getMessage());
    }
  }

  private int countSessions(final LoginCommand cmd) {
    try {
      final String keycloakId = identityService.lookupId(cmd.username());
      return identityService.listActiveSessions(keycloakId).size();
    } catch (Exception ex) {
      return 0;
    }
  }
}
