package com.chamrong.iecommerce.review.domain.exception;

/** Thrown when a duplicate vote is attempted for the same (tenant, review, voter) tuple. */
public class DuplicateVoteException extends ReviewDomainException {

  public DuplicateVoteException(String message) {
    super(message);
  }
}
