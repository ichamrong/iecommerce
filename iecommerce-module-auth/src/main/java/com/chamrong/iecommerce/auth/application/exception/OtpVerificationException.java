package com.chamrong.iecommerce.auth.application.exception;

/**
 * Thrown when a submitted OTP code is invalid, expired, or already consumed.
 *
 * <p>Maps to HTTP 401 Unauthorized in {@link
 * com.chamrong.iecommerce.auth.api.AuthExceptionHandler}. No specific reason is given to the client
 * to prevent oracle attacks (attacker could distinguish "wrong code" vs "expired code" to refine
 * guessing strategy).
 */
public class OtpVerificationException extends RuntimeException {

  public OtpVerificationException(final String message) {
    super(message);
  }
}
