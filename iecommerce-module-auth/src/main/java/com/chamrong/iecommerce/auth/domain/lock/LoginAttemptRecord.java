package com.chamrong.iecommerce.auth.domain.lock;

import java.time.Duration;
import java.time.Instant;

/**
 * Immutable snapshot of a user's login attempt history.
 *
 * <p>This is a <strong>value object</strong> — not a JPA entity. Stored in the {@link
 * LoginLockStore}, which is either Caffeine (single-node) or Redis (distributed).
 *
 * @param username the username key (not hashed — kept for key lookup only)
 * @param tenantId tenant scope (prevents cross-tenant lock collisions)
 * @param failedAttempts total consecutive failed logins since last successful login or reset
 * @param lockedUntil when the temporary lock expires; {@code null} if not currently locked
 * @param lastAttemptAt timestamp of the most recent login attempt (success or failure)
 */
public record LoginAttemptRecord(
    String username,
    String tenantId,
    int failedAttempts,
    Instant lockedUntil,
    Instant lastAttemptAt) {

  /** A clean record with zero failed attempts — used as the initial or post-reset state. */
  public static LoginAttemptRecord clean(final String username, final String tenantId) {
    return new LoginAttemptRecord(username, tenantId, 0, null, Instant.now());
  }

  /** Returns {@code true} if the account is currently within a lock window. */
  public boolean isLocked() {
    return lockedUntil != null && Instant.now().isBefore(lockedUntil);
  }

  /**
   * How long the lock is still active.
   *
   * @return remaining lock duration, or {@link Duration#ZERO} if not locked
   */
  public Duration remainingLockDuration() {
    if (!isLocked()) {
      return Duration.ZERO;
    }
    return Duration.between(Instant.now(), lockedUntil);
  }

  /**
   * Returns a new record with one more failed attempt and the lock applied according to the
   * supplied policy.
   *
   * @param policy the progressive backoff policy to consult for next lock duration
   */
  public LoginAttemptRecord recordFailure(final LoginLockPolicy policy) {
    final int newCount = failedAttempts + 1;
    final Duration lockDuration = policy.lockDurationFor(newCount);
    final Instant newLockedUntil = lockDuration.isZero() ? null : Instant.now().plus(lockDuration);
    return new LoginAttemptRecord(username, tenantId, newCount, newLockedUntil, Instant.now());
  }

  /** Returns a clean record — resets all counters after a successful login. */
  public LoginAttemptRecord recordSuccess() {
    return clean(username, tenantId);
  }
}
