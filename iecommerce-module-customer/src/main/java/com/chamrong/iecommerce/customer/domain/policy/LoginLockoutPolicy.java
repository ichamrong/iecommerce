package com.chamrong.iecommerce.customer.domain.policy;

import java.time.Duration;

/**
 * Progressive lockout policy: 1m, 3m, 5m backoff after consecutive login failures. ASVS L2-grade.
 */
public interface LoginLockoutPolicy {

  /**
   * Returns lock duration for the given consecutive failure count. Zero means no lock.
   *
   * @param consecutiveFailures number of consecutive failures
   * @return lock duration (e.g. 1m, 3m, 5m) or ZERO
   */
  Duration getLockDuration(int consecutiveFailures);

  /** Lock duration in minutes for use with LoginAttempt.registerFailure(minutes, now). */
  default int getLockDurationMinutes(int consecutiveFailures) {
    Duration d = getLockDuration(consecutiveFailures);
    return (int) d.toMinutes();
  }
}
