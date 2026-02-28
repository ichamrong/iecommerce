package com.chamrong.iecommerce.customer.domain.auth;

import java.time.Duration;
import java.time.Instant;

public class AccountState {
  private final String customerId;
  private int consecutiveFailures;
  private Instant lockedUntil;

  public AccountState(String customerId, int consecutiveFailures, Instant lockedUntil) {
    this.customerId = customerId;
    this.consecutiveFailures = consecutiveFailures;
    this.lockedUntil = lockedUntil;
  }

  public String getCustomerId() {
    return customerId;
  }

  public int getConsecutiveFailures() {
    return consecutiveFailures;
  }

  public Instant getLockedUntil() {
    return lockedUntil;
  }

  public void registerFailure(LoginLockPolicy policy, Instant now) {
    this.consecutiveFailures++;
    Duration lockDuration = policy.calculateLock(this.consecutiveFailures);
    if (!lockDuration.isZero()) {
      this.lockedUntil = now.plus(lockDuration);
    }
  }

  public void registerSuccess() {
    this.consecutiveFailures = 0;
    this.lockedUntil = null;
  }

  public boolean isLocked(Instant now) {
    return lockedUntil != null && now.isBefore(lockedUntil);
  }
}
