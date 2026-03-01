package com.chamrong.iecommerce.payment.domain.exception;

/** Thrown when a payment state transition is not permitted by the domain state machine. */
public class IllegalPaymentTransitionException extends PaymentDomainException {

  public IllegalPaymentTransitionException(Object from, Object to) {
    super("Illegal payment state transition: %s → %s".formatted(from, to));
  }
}
