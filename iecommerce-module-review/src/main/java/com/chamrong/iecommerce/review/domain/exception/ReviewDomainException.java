package com.chamrong.iecommerce.review.domain.exception;

/** Base exception type for review domain errors. */
public class ReviewDomainException extends RuntimeException {

  public ReviewDomainException(String message) {
    super(message);
  }

  public ReviewDomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
