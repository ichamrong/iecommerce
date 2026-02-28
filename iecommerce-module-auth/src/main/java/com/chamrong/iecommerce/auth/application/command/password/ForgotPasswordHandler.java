package com.chamrong.iecommerce.auth.application.command.password;

import com.chamrong.iecommerce.auth.application.audit.AuthEventLogger;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.UserRepository;
import com.chamrong.iecommerce.auth.domain.lock.LoginLockStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Triggers a Keycloak "Reset Password" email for the given user.
 *
 * <h3>Security design (OWASP A07 — user enumeration prevention)</h3>
 *
 * <p>This handler <em>always returns successfully</em>, even when no account is found for the
 * supplied email. The caller (controller) must always respond with {@code 200 OK} so that attackers
 * cannot determine whether an email address is registered.
 *
 * <h3>Session invalidation on password reset</h3>
 *
 * <p>After the reset email is triggered, <strong>all active Keycloak sessions for the user are
 * revoked</strong>. This ensures:
 *
 * <ul>
 *   <li>Any existing JWT tokens become immediately unusable (Keycloak rejects them because the
 *       backingSession no longer exists in its session store).
 *   <li>No "token versioning" complexity — Keycloak's own session store is the source of truth.
 *   <li>An attacker who obtained an old access token cannot continue using it after a reset.
 * </ul>
 *
 * <h3>Lock counter reset</h3>
 *
 * <p>The progressive login lock counter is also cleared after a successful reset trigger.
 * Rationale: if the user forgot their password (likely the cause of repeated failures), they should
 * be able to log in cleanly with the new password without a residual lock.
 */
@Component
@RequiredArgsConstructor
public class ForgotPasswordHandler {

  private final UserRepository userRepository;
  private final IdentityService identityService;
  private final LoginLockStore lockStore;
  private final AuthEventLogger auditLog;

  /**
   * Sends a Keycloak-managed password-reset email if the user exists, then revokes all active
   * sessions and clears the lock counter.
   *
   * @param cmd the forgot-password command containing email and tenantId
   */
  public void handle(final ForgotPasswordCommand cmd) {
    userRepository
        .findByEmailAndTenantId(cmd.email(), cmd.tenantId())
        .ifPresentOrElse(
            user -> {
              if (user.getKeycloakId() == null) {
                auditLog.forgotPasswordTriggered(cmd.tenantId(), false);
                return;
              }

              // 1. Trigger Keycloak reset email
              identityService.sendPasswordResetEmail(user.getKeycloakId());

              // 2. Invalidate ALL existing sessions immediately
              //    This makes any outstanding JWTs unusable — they reference sessions
              //    that no longer exist in Keycloak's session store.
              final java.util.List<com.chamrong.iecommerce.auth.domain.UserSession> activeSessions =
                  identityService.listActiveSessions(user.getKeycloakId());
              identityService.revokeAllSessions(user.getKeycloakId());

              auditLog.sessionsInvalidated(
                  user.getUsername(), cmd.tenantId(), activeSessions.size(), "forgot-password");

              // 3. Clear progressive lock counter so the user can log in cleanly
              //    with the new password (lock was likely caused by forgotten password)
              lockStore.clear(user.getUsername(), cmd.tenantId());

              auditLog.forgotPasswordTriggered(cmd.tenantId(), true);
            },
            () -> {
              // Silent no-op — do NOT reveal that the email is unknown (OWASP A07)
              auditLog.forgotPasswordTriggered(cmd.tenantId(), false);
            });
  }
}
