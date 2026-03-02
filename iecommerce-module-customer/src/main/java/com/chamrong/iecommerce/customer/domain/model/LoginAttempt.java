package com.chamrong.iecommerce.customer.domain.model;

import java.time.Instant;

/**
 * Tracks login attempt state for lockout: consecutive failures and locked-until. Used by
 * LoginAttemptPort; no JPA.
 */
public class LoginAttempt {

  private final String customerId;
  private int consecutiveFailures;
  private Instant lockedUntil;

  public LoginAttempt(String customerId, int consecutiveFailures, Instant lockedUntil) {
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

  public boolean isLocked(Instant now) {
    return lockedUntil != null && now.isBefore(lockedUntil);
  }

  /**
   * Record a failed attempt. Caller provides lock duration in minutes (from LoginLockoutPolicy).
   */
  public void registerFailure(int lockDurationMinutes, Instant now) {
    this.consecutiveFailures++;
    if (lockDurationMinutes > 0) {
      this.lockedUntil = now.plusSeconds(lockDurationMinutes * 60L);
    }
  }

  public void registerSuccess() {
    this.consecutiveFailures = 0;
    this.lockedUntil = null;
  }
}
