package com.chamrong.iecommerce.payment.domain.shared;

import com.chamrong.iecommerce.payment.domain.exception.IllegalPaymentTransitionException;

/**
 * Normalized lifecycle states of a Payment Intent.
 *
 * <p>State machine rules: see {@link #assertCanTransitionTo(PaymentStatus)}.
 */
public enum PaymentStatus {

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

  /** Returns true for states that permit no further transitions. */
  public boolean isTerminal() {
    return this == SUCCEEDED || this == FAILED || this == CANCELLED || this == EXPIRED;
  }

  public boolean canTransitionTo(PaymentStatus next) {
    if (this == SUCCEEDED) return next == REFUNDED;
    if (this.isTerminal()) return false;
    return true;
  }

  /** Asserts the transition is valid or throws {@link IllegalPaymentTransitionException}. */
  public void assertCanTransitionTo(PaymentStatus next) {
    if (!canTransitionTo(next)) {
      throw new IllegalPaymentTransitionException(this, next);
    }
  }
}
