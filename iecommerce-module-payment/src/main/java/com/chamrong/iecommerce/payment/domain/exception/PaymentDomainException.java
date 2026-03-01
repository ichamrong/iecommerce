package com.chamrong.iecommerce.payment.domain.exception;

/**
 * Base domain exception for the payment bounded context.
 *
 * <p>Throw this (or a more specific subclass) for any domain invariant violation. Infrastructure
 * adapters should catch this and map it to the appropriate HTTP status.
 */
public class PaymentDomainException extends RuntimeException {

  public PaymentDomainException(String message) {
    super(message);
  }

  public PaymentDomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
