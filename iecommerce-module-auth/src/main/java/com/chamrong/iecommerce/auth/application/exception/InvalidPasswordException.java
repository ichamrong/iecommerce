package com.chamrong.iecommerce.auth.application.exception;

import com.chamrong.iecommerce.auth.domain.exception.AuthErrorCode;
import com.chamrong.iecommerce.auth.domain.exception.AuthException;

public class InvalidPasswordException extends AuthException {
  public InvalidPasswordException(String message) {
    super(AuthErrorCode.INVALID_CREDENTIALS, message);
  }
}
