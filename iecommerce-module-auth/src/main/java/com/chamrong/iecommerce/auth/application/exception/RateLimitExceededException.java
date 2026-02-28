package com.chamrong.iecommerce.auth.application.exception;

/**
 * Thrown when a client IP address exceeds the configured request rate limit.
 *
 * <p>Mapped to HTTP 429 Too Many Requests by {@code AuthExceptionHandler}.
 */
public class RateLimitExceededException extends RuntimeException {

  public RateLimitExceededException(String message) {
    super(message);
  }
}
