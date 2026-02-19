package com.chamrong.iecommerce.payment.domain;

public enum PaymentStatus {
  PENDING,
  AUTHORIZED,
  CAPTURED,
  FAILED,
  REFUNDED,
  CANCELLED
}
