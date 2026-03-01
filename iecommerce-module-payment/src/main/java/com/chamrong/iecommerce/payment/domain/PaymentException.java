package com.chamrong.iecommerce.payment.domain;

public class PaymentException extends RuntimeException {
  public PaymentException(String message) {
    super(message);
  }
}
