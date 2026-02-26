package com.chamrong.iecommerce.order.infrastructure;

import com.chamrong.iecommerce.common.event.InventoryOperationFailedEvent;
import com.chamrong.iecommerce.common.event.PaymentFailedEvent;
import com.chamrong.iecommerce.common.event.PaymentSucceededEvent;
import com.chamrong.iecommerce.common.event.StockReservedEvent;
import com.chamrong.iecommerce.order.application.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saga Orchestrator (choreography-based) for the Order module. Listens to events from Inventory and
 * Payment modules to progress the Order lifecycle.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaListener {

  private final OrderService orderService;

  @EventListener
  @Transactional
  public void onStockReserved(StockReservedEvent event) {
    log.info("Saga [Order]: Stock reserved for order {}. Ready for payment.", event.orderId());
    // In a fully automated flow, we might move to 'ArrangingPayment' here.
    // In this app, we stay in 'Confirmed' until payment succeeds.
  }

  @EventListener
  @Transactional
  public void onInventoryFailure(InventoryOperationFailedEvent event) {
    log.error(
        "Saga [Order]: Inventory failure for order {}: {}. Cancelling order.",
        event.orderId(),
        event.reason());
    orderService.cancel(event.orderId());
  }

  @EventListener
  @Transactional
  public void onPaymentSucceeded(PaymentSucceededEvent event) {
    log.info(
        "Saga [Order]: Payment succeeded for order {}. Transitioning to Picking.", event.orderId());
    orderService.pick(event.orderId());
    // Optionally: Mark order as settled/paid
  }

  @EventListener
  @Transactional
  public void onPaymentFailed(PaymentFailedEvent event) {
    log.warn(
        "Saga [Order]: Payment failed for order {}: {}. Cancelling order.",
        event.orderId(),
        event.reason());
    orderService.cancel(event.orderId());
    // orderService.cancel will emit OrderCancelledEvent, which InventorySagaListener
    // already handles by releasing stock. Distributed rollback complete!
  }
}
