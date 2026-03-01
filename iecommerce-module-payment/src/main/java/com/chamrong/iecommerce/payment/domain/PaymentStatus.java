package com.chamrong.iecommerce.payment.domain;

/** Normalized lifecycle states of a Payment Intent. */
public enum PaymentStatus {
  /** Intent created, no money moved yet. */
  /** Intent created, no money moved yet. */
  CREATED,

  /** Intent is pending (legacy/wait status). */
  PENDING,

  /** Waiting for customer action (3DS, PayPal redirect, etc). */
  REQUIRES_ACTION,

  /** Provider is processing the payment (e.g. ACH, pending bank transfer). */
  PROCESSING,

  /** Payment successfully authorized/captured. */
  SUCCEEDED,

  /** Payment failed (declined, insufficient funds, etc). */
  FAILED,

  /** Intent was cancelled before completion. */
  CANCELLED,

  /** Intent expired without completion. */
  EXPIRED,

  /** Payment was fully or partially refunded. */
  REFUNDED;

  public boolean isTerminal() {
    return this == SUCCEEDED || this == FAILED || this == CANCELLED || this == EXPIRED;
  }

  public boolean canTransitionTo(PaymentStatus next) {
    if (this == SUCCEEDED) return next == REFUNDED;
    if (this.isTerminal()) return false;
    return true;
  }
}
