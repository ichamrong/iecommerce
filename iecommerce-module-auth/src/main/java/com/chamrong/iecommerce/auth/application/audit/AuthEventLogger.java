package com.chamrong.iecommerce.auth.application.audit;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Centralised structured auth event logger.
 *
 * <h3>Why a dedicated logger?</h3>
 *
 * <p>Scattering {@code log.info/warn} calls across handlers makes it hard to:
 *
 * <ul>
 *   <li>Find all security log statements at once (SIEM query coverage).
 *   <li>Ensure consistent fields (every security event must carry the same MDC keys).
 *   <li>Add a routing appender later (e.g. all SECURITY-level events → separate file / Kafka).
 * </ul>
 *
 * <h3>What is NOT logged</h3>
 *
 * <ul>
 *   <li>Passwords — never, under any circumstance.
 *   <li>Full JWT tokens — log correlation IDs instead (e.g. {@code jti} claim).
 *   <li>OTP codes — never. Only the fact that an OTP was sent or verified.
 * </ul>
 *
 * <h3>MDC fields</h3>
 *
 * <p>Every log call expects {@code requestId}, {@code tenantId}, and {@code clientIp} to already be
 * in the MDC (set by {@link com.chamrong.iecommerce.auth.infrastructure.aop.MdcLoggingFilter}).
 * Additional event-specific fields are set temporarily and cleared after the log call.
 */
@Component
public class AuthEventLogger {

  /** Dedicated logger — can be routed to a separate appender in logback-spring.xml. */
  private static final Logger SECURITY_LOG = LoggerFactory.getLogger("SECURITY_AUDIT");

  // ─── Login ────────────────────────────────────────────────────────────────

  /**
   * Logs a successful login.
   *
   * @param username the logged-in username (not masked — needed for audit)
   * @param tenantId owning tenant
   * @param sessionCount number of active sessions after this login
   */
  public void loginSuccess(final String username, final String tenantId, final int sessionCount) {
    SECURITY_LOG.info(
        "[AUTH] LOGIN_SUCCESS username={} tenant={} activeSessions={}",
        username,
        tenantId,
        sessionCount);
  }

  /**
   * Logs a failed login attempt (wrong password, locked account, etc.).
   *
   * @param username the attempted username
   * @param tenantId owning tenant
   * @param reason human-readable failure reason — must NOT contain credential data
   * @param failedAttempts total consecutive failed attempts so far
   */
  public void loginFailure(
      final String username, final String tenantId, final String reason, final int failedAttempts) {
    SECURITY_LOG.warn(
        "[AUTH] LOGIN_FAILURE username={} tenant={} reason={} consecutiveFailures={}",
        username,
        tenantId,
        reason,
        failedAttempts);
  }

  /**
   * Logs when a progressive lock is applied to a username.
   *
   * @param username the locked username
   * @param tenantId owning tenant
   * @param lockDuration how long the lock lasts
   * @param attempts total failed attempts that triggered this lock
   */
  public void accountLocked(
      final String username,
      final String tenantId,
      final Duration lockDuration,
      final int attempts) {
    SECURITY_LOG.warn(
        "[AUTH] ACCOUNT_LOCKED username={} tenant={} lockDurationSeconds={} attempts={}",
        username,
        tenantId,
        lockDuration.toSeconds(),
        attempts);
  }

  /**
   * Logs a login attempt against a currently locked account.
   *
   * @param username the username attempting to log in
   * @param tenantId owning tenant
   * @param remainingDuration how long until the lock expires
   */
  public void loginRejectedLocked(
      final String username, final String tenantId, final Duration remainingDuration) {
    SECURITY_LOG.warn(
        "[AUTH] LOGIN_REJECTED_LOCKED username={} tenant={} remainingLockSeconds={}",
        username,
        tenantId,
        remainingDuration.toSeconds());
  }

  /**
   * Logs a concurrent session detection event.
   *
   * @param username the username
   * @param tenantId owning tenant
   * @param existingSessionCount number of sessions already active
   * @param newSessionIp IP of the new login
   */
  public void concurrentSessionDetected(
      final String username,
      final String tenantId,
      final int existingSessionCount,
      final String newSessionIp) {
    SECURITY_LOG.warn(
        "[AUTH] CONCURRENT_SESSION username={} tenant={} existingSessions={} newIp={}",
        username,
        tenantId,
        existingSessionCount,
        newSessionIp);
  }

  /**
   * Logs a session invalidation (logout / forget-password revoke).
   *
   * @param username the affected username
   * @param tenantId owning tenant
   * @param sessionCount number of sessions that were revoked
   * @param reason why sessions were invalidated (e.g. "forgot-password", "admin-revoke")
   */
  public void sessionsInvalidated(
      final String username, final String tenantId, final int sessionCount, final String reason) {
    SECURITY_LOG.info(
        "[AUTH] SESSIONS_INVALIDATED username={} tenant={} revokedCount={} reason={}",
        username,
        tenantId,
        sessionCount,
        reason);
  }

  /**
   * Logs a forgot-password request (always fires, even for unknown emails — value is in the audit
   * trail, not in knowing whether the account exists).
   *
   * @param tenantId owning tenant
   * @param found whether a matching user was found (avoids logging the email for privacy)
   */
  public void forgotPasswordTriggered(final String tenantId, final boolean found) {
    if (found) {
      SECURITY_LOG.info("[AUTH] FORGOT_PASSWORD_TRIGGERED tenant={} userFound=true", tenantId);
    } else {
      // Logged at debug — the email is not logged to prevent user-enumeration via logs
      SECURITY_LOG.debug(
          "[AUTH] FORGOT_PASSWORD_TRIGGERED tenant={} userFound=false (silent no-op)", tenantId);
    }
  }
}
