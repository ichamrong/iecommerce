package com.chamrong.iecommerce.customer.domain.auth;

import java.time.Duration;

public interface LoginLockPolicy {
  /**
   * Calculates the duration of the lock based on the number of consecutive failures. Returns
   * Duration.ZERO if no lock should be applied.
   */
  Duration calculateLock(int consecutiveFailures);
}
