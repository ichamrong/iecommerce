package com.chamrong.iecommerce.review.domain.exception;

/** Thrown when a caller attempts to access or modify a review without sufficient rights. */
public class UnauthorizedReviewAccessException extends ReviewDomainException {

  public UnauthorizedReviewAccessException(String message) {
    super(message);
  }
}
