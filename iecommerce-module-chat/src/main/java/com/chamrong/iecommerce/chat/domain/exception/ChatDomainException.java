package com.chamrong.iecommerce.chat.domain.exception;

/** Base exception for chat domain rule violations (access denied, validation, duplicate). */
public class ChatDomainException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ChatDomainException(String message) {
    super(message);
  }

  public ChatDomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
