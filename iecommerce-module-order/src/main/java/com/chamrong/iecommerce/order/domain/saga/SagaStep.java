package com.chamrong.iecommerce.order.domain.saga;

/**
 * Ordered steps of the Order saga choreography.
 *
 * <p>The saga transitions as external events arrive:
 *
 * <pre>
 *   CREATE_ORDER
 *     → RESERVE_INVENTORY  (OrderConfirmedEvent → Inventory)
 *     → AWAIT_PAYMENT      (StockReservedEvent arrives)
 *     → COMMIT_INVENTORY   (PaymentSucceededEvent arrives)
 *     → COMPLETE           (OrderCompletedEvent fired)
 *
 *   Compensation path:
 *     RESERVE_INVENTORY failed → CANCEL_ORDER
 *     AWAIT_PAYMENT failed     → RELEASE_INVENTORY → CANCEL_ORDER
 * </pre>
 */
public enum SagaStep {
  CREATE_ORDER,
  RESERVE_INVENTORY,
  AWAIT_PAYMENT,
  COMMIT_INVENTORY,
  COMPLETE,
  RELEASE_INVENTORY, // compensation: undo reservation
  CANCEL_ORDER // compensation: terminal
}
