package com.chamrong.iecommerce.order.domain;

import java.util.Set;

/**
 * Thrown when an order state transition is forbidden by {@link OrderStateMachine}.
 *
 * <p>Using a dedicated exception type (rather than a generic {@link IllegalStateException}) allows
 * callers to catch and handle this specific case — for example, the REST layer maps it to HTTP 409
 * Conflict, which is the correct semantic for a state conflict.
 */
public final class OrderTransitionException extends RuntimeException {

  private final OrderState from;
  private final OrderState to;
  private final Set<OrderState> allowed;

  public OrderTransitionException(
      final OrderState from, final OrderState to, final Set<OrderState> allowed) {
    super(
        String.format(
            "Illegal order state transition: %s → %s. Allowed targets from %s: %s",
            from, to, from, allowed));
    this.from = from;
    this.to = to;
    this.allowed = allowed;
  }

  public OrderState getFrom() {
    return from;
  }

  public OrderState getTo() {
    return to;
  }

  public Set<OrderState> getAllowed() {
    return allowed;
  }
}
