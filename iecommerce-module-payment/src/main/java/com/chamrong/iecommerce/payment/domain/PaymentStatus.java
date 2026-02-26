package com.chamrong.iecommerce.payment.domain;

/** Lifecycle status of a payment transaction. */
public enum PaymentStatus {
  PENDING,
  AUTHORIZED,
  SUCCEEDED,
  CAPTURED,
  FAILED,
  REFUNDED,
  CANCELLED
}
