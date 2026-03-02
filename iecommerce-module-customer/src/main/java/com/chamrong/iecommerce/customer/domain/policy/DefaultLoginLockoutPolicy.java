package com.chamrong.iecommerce.customer.domain.policy;

import java.time.Duration;

/** Default progressive backoff: 3 failures = 1m, 5 = 3m, 7+ = 5m. */
public final class DefaultLoginLockoutPolicy implements LoginLockoutPolicy {

  @Override
  public Duration getLockDuration(int consecutiveFailures) {
    if (consecutiveFailures >= 7) return Duration.ofMinutes(5);
    if (consecutiveFailures >= 5) return Duration.ofMinutes(3);
    if (consecutiveFailures >= 3) return Duration.ofMinutes(1);
    return Duration.ZERO;
  }
}
