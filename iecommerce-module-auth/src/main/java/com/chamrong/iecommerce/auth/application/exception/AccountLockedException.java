package com.chamrong.iecommerce.auth.application.exception;

import java.time.Duration;
import lombok.Getter;

/**
 * Thrown when a login attempt is rejected because the account is temporarily locked.
 *
 * <p>Maps to HTTP <strong>429 Too Many Requests</strong> (not 401 or 423). The credentials may be
 * correct, but the progressive lock policy prevents access. Returning 429 signals "try again later"
 * rather than "wrong credentials" — this is intentional to avoid leaking credential correctness.
 *
 * <p>The {@link #remainingLockDuration} can be used to generate a standard {@code Retry-After}
 * response header.
 */
@Getter
public class AccountLockedException extends RuntimeException {

  /**
   * -- GETTER -- How long until the account unlocks itself. Use this to generate a response header.
   */
  private final Duration remainingLockDuration;

  public AccountLockedException(final String username, final Duration remainingLockDuration) {
    super(
        String.format(
            "Account temporarily locked. Try again in %d seconds.",
            remainingLockDuration.toSeconds()));
    this.remainingLockDuration = remainingLockDuration;
  }

  /** Legacy constructor — use the two-arg version where possible. */
  public AccountLockedException(final String message) {
    super(message);
    this.remainingLockDuration = Duration.ZERO;
  }
}
