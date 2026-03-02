package com.chamrong.iecommerce.review.domain.exception;

/** Thrown when abuse prevention policies detect excessive or spammy activity. */
public class AbuseLimitExceededException extends ReviewDomainException {

  public AbuseLimitExceededException(String message) {
    super(message);
  }
}
