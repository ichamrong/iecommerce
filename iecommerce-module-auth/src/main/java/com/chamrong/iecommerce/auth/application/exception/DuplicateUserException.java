package com.chamrong.iecommerce.auth.application.exception;

import com.chamrong.iecommerce.auth.domain.exception.AuthErrorCode;
import com.chamrong.iecommerce.auth.domain.exception.AuthException;

/** Thrown when attempting to register a user with an already-taken username or email. */
public class DuplicateUserException extends AuthException {
  public DuplicateUserException(String message) {
    super(AuthErrorCode.DUPLICATE_USER, message);
  }
}
