package com.chamrong.iecommerce.auth.domain.lock;

import java.time.Duration;

/**
 * Progressive backoff policy for username-level login locking.
 *
 * <h3>Design: Strategy Pattern</h3>
 *
 * <p>Encodes "how long to lock given N consecutive failures." Different policies can be injected
 * for different tenant tiers or environments without touching the lock enforcement logic.
 *
 * <h3>Default schedule</h3>
 *
 * <pre>
 *   Attempts 1-2  → no lock (just count)
 *   Attempt  3    → 1 minute
 *   Attempt  4    → 1 minute
 *   Attempt  5    → 3 minutes
 *   Attempt  6    → 3 minutes
 *   Attempt  7+   → 5 minutes
 * </pre>
 *
 * <p>The schedule is intentionally not exponential (which would reach impractical values quickly).
 * It is a configurable step function — override via {@link LoginLockConfig} properties.
 */
public interface LoginLockPolicy {

  /**
   * Returns the lock duration for the given number of consecutive failed attempts.
   *
   * @param failedAttempts total consecutive failures (1-based)
   * @return the lock duration; {@link Duration#ZERO} means "do not lock yet"
   */
  Duration lockDurationFor(int failedAttempts);

  /**
   * Returns the threshold at which locking begins. Attempts below this threshold are counted but no
   * lock is applied.
   */
  int lockThreshold();
}
