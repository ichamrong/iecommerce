package com.chamrong.iecommerce.auth.domain.exception;

import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
public class AuthException extends RuntimeException {

  private final AuthErrorCode errorCode;

  public AuthException(AuthErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public AuthException(AuthErrorCode errorCode, String message) {
    super(errorCode.getMessage() + ": " + message);
    this.errorCode = errorCode;
  }

  public AuthException(AuthErrorCode errorCode, String message, @Nullable Throwable cause) {
    super(errorCode.getMessage() + ": " + message, cause);
    this.errorCode = errorCode;
  }
}
