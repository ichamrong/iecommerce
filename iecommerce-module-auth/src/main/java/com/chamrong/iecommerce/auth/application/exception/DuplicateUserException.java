package com.chamrong.iecommerce.auth.application.exception;

/** Thrown when attempting to register a user with an already-taken username or email. */
public class DuplicateUserException extends RuntimeException {
  public DuplicateUserException(String message) {
    super(message);
  }
}
