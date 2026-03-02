package com.chamrong.iecommerce.review.domain.exception;

/** Thrown when a review target type or id is invalid for the current tenant. */
public class InvalidReviewTargetException extends ReviewDomainException {

  public InvalidReviewTargetException(String message) {
    super(message);
  }
}
