package com.chamrong.iecommerce.customer.domain.auth;

import java.time.Duration;

public class DefaultLoginLockPolicy implements LoginLockPolicy {

  @Override
  public Duration calculateLock(int consecutiveFailures) {
    if (consecutiveFailures >= 7) {
      return Duration.ofMinutes(5);
    } else if (consecutiveFailures >= 5) {
      return Duration.ofMinutes(3);
    } else if (consecutiveFailures >= 3) {
      return Duration.ofMinutes(1);
    }
    return Duration.ZERO;
  }
}
