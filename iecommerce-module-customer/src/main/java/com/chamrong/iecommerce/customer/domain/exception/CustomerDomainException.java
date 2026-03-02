package com.chamrong.iecommerce.customer.domain.exception;

/**
 * Base exception for customer domain violations (e.g. duplicate email, invalid state transition).
 */
public class CustomerDomainException extends RuntimeException {

  private final String errorCode;

  public CustomerDomainException(String message) {
    super(message);
    this.errorCode = "CUSTOMER_DOMAIN_ERROR";
  }

  public CustomerDomainException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
