package com.chamrong.iecommerce.order.infrastructure.messaging;

import com.chamrong.iecommerce.common.event.InventoryOperationFailedEvent;
import com.chamrong.iecommerce.common.event.PaymentFailedEvent;
import com.chamrong.iecommerce.common.event.PaymentSucceededEvent;
import com.chamrong.iecommerce.common.event.StockReservedEvent;
import com.chamrong.iecommerce.order.application.OrderService;
import com.chamrong.iecommerce.order.domain.OrderAuditActions;
import com.chamrong.iecommerce.order.domain.OrderState;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import com.chamrong.iecommerce.order.domain.ports.OrderSagaStatePort;
import com.chamrong.iecommerce.order.domain.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Listens for events from other modules to advance the Order Saga choreography. */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaListener {

  private final OrderRepositoryPort orderRepository;
  private final OrderAuditPort auditPort;
  private final OrderSagaStatePort sagaStatePort;
  private final OrderService orderService;

  /** Step 2: Inventory reserved successfully. Pass control to Payment. */
  @EventListener
  @Transactional
  public void handleStockReserved(StockReservedEvent event) {
    log.info("Received StockReservedEvent for order {}", event.orderId());

    orderRepository
        .findByIdForUpdate(event.orderId())
        .ifPresent(
            order -> {
              OrderState prev = order.getState();
              order.arrangePayment(); // Transitions AddingItems -> ArrangingPayment
              orderRepository.save(order);

              auditPort.log(
                  order.getId(),
                  order.getTenantId(),
                  prev,
                  order.getState(),
                  OrderAuditActions.ORDER_ARRANGING_PAYMENT,
                  "system",
                  "inventory_reserved");

              sagaStatePort.upsert(order.getId(), SagaStep.AWAIT_PAYMENT, "RUNNING");
            });
  }

  /** Step 3: Payment authorized successfully. Complete the confirmation. */
  @EventListener
  @Transactional
  public void handlePaymentSucceeded(PaymentSucceededEvent event) {
    log.info("Received PaymentSucceededEvent for order {}", event.orderId());

    orderRepository
        .findByIdForUpdate(event.orderId())
        .ifPresent(
            order -> {
              OrderState prev = order.getState();
              order.authorizePayment(); // Transitions ArrangingPayment -> PaymentAuthorized
              orderRepository.save(order);

              auditPort.log(
                  order.getId(),
                  order.getTenantId(),
                  prev,
                  order.getState(),
                  OrderAuditActions.ORDER_PAYMENT_AUTHORIZED,
                  "system",
                  "payment_id=" + event.paymentId());

              sagaStatePort.upsert(order.getId(), SagaStep.COMPLETE, "DONE");
            });
  }

  /** Saga compensation: inventory failure — cancel the order. */
  @EventListener
  @Transactional
  public void handleInventoryFailure(InventoryOperationFailedEvent event) {
    log.error(
        "Saga [Order]: Inventory failure for order {}: {}. Cancelling order.",
        event.orderId(),
        event.reason());
    orderService.cancel(event.orderId());
  }

  /** Saga compensation: payment failure — cancel the order. */
  @EventListener
  @Transactional
  public void handlePaymentFailed(PaymentFailedEvent event) {
    log.warn(
        "Saga [Order]: Payment failed for order {}: {}. Cancelling order.",
        event.orderId(),
        event.reason());
    orderService.cancel(event.orderId());
  }
}
