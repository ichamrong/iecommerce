package com.chamrong.iecommerce.order.domain;

/**
 * Lifecycle states of an order.
 *
 * <p>Allowed transitions:
 *
 * <pre>
 *   AddingItems → ArrangingPayment → PaymentAuthorized → PaymentSettled → Shipped → Delivered
 *   Any non-terminal state → Cancelled
 * </pre>
 */
public enum OrderState {
  AddingItems,
  ArrangingPayment,
  PaymentAuthorized,
  PaymentSettled,
  Confirmed,
  Picking,
  Packing,
  Shipped,
  Delivered,
  Completed,
  Cancelled
}
