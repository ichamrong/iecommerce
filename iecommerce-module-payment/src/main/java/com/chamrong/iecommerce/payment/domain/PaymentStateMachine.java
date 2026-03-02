package com.chamrong.iecommerce.payment.domain;

/**
 * Explicit state machine for payment lifecycle transitions.
 *
 * <p>Enforces the allowed transitions and throws {@link PaymentException} for illegal moves.
 */
public final class PaymentStateMachine {

  private PaymentStateMachine() {}

  public static PaymentStatus onCreatedToRequiresAction(PaymentStatus current) {
    require(current == PaymentStatus.CREATED || current == PaymentStatus.PENDING, current);
    return PaymentStatus.REQUIRES_ACTION;
  }

  public static PaymentStatus onAuthorizedOrCaptured(PaymentStatus current) {
    if (current == PaymentStatus.SUCCEEDED) {
      return current;
    }
    if (current == PaymentStatus.CREATED
        || current == PaymentStatus.PENDING
        || current == PaymentStatus.REQUIRES_ACTION
        || current == PaymentStatus.PROCESSING) {
      return PaymentStatus.SUCCEEDED;
    }
    throw illegalTransition(current, PaymentStatus.SUCCEEDED);
  }

  public static PaymentStatus onFailure(PaymentStatus current) {
    if (current == PaymentStatus.SUCCEEDED || current == PaymentStatus.REFUNDED) {
      throw illegalTransition(current, PaymentStatus.FAILED);
    }
    if (current.isTerminal()) {
      return current;
    }
    return PaymentStatus.FAILED;
  }

  public static PaymentStatus onCancel(PaymentStatus current) {
    if (current.isTerminal()) {
      return current;
    }
    if (current == PaymentStatus.SUCCEEDED || current == PaymentStatus.REFUNDED) {
      throw illegalTransition(current, PaymentStatus.CANCELLED);
    }
    return PaymentStatus.CANCELLED;
  }

  public static PaymentStatus onExpire(PaymentStatus current) {
    if (current.isTerminal()) {
      return current;
    }
    if (current == PaymentStatus.SUCCEEDED || current == PaymentStatus.REFUNDED) {
      throw illegalTransition(current, PaymentStatus.EXPIRED);
    }
    return PaymentStatus.EXPIRED;
  }

  public static PaymentStatus onRefund(PaymentStatus current) {
    if (current == PaymentStatus.REFUNDED) {
      return current;
    }
    if (current != PaymentStatus.SUCCEEDED) {
      throw illegalTransition(current, PaymentStatus.REFUNDED);
    }
    return PaymentStatus.REFUNDED;
  }

  private static void require(boolean expression, PaymentStatus current) {
    if (!expression) {
      throw new PaymentException("Illegal transition from " + current);
    }
  }

  private static PaymentException illegalTransition(PaymentStatus from, PaymentStatus to) {
    return new PaymentException("Illegal payment state transition " + from + " -> " + to);
  }
}
