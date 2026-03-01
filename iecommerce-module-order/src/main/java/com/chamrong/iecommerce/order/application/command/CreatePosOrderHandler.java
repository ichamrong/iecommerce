package com.chamrong.iecommerce.order.application.command;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.event.OrderCompletedEvent;
import com.chamrong.iecommerce.common.event.OrderShippedEvent;
import com.chamrong.iecommerce.order.application.dto.CreatePosOrderRequest;
import com.chamrong.iecommerce.order.application.dto.OrderResponse;
import com.chamrong.iecommerce.order.application.dto.OrderResponse.OrderItemResponse;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderAuditActions;
import com.chamrong.iecommerce.order.domain.OrderItem;
import com.chamrong.iecommerce.order.domain.OrderState;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import com.chamrong.iecommerce.order.domain.ports.OrderOutboxPort;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import com.chamrong.iecommerce.order.domain.ports.OrderSagaStatePort;
import com.chamrong.iecommerce.order.domain.saga.SagaStep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: Create and immediately complete a POS order. Bypasses long-running fulfillment steps.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreatePosOrderHandler {

  private final OrderRepositoryPort orderRepository;
  private final OrderAuditPort auditPort;
  private final OrderOutboxPort outboxPort;
  private final OrderSagaStatePort sagaStatePort;
  private final ObjectMapper objectMapper;
  private final MeterRegistry metrics;

  @Transactional
  public OrderResponse handle(String tenantId, CreatePosOrderRequest req, String actor) {
    final Order order = new Order();
    order.assignTenantId(tenantId);
    order.assignCode("POS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    order.setCustomerId(req.customerId());

    for (var itemReq : req.items()) {
      order.addItem(
          OrderItem.of(
              itemReq.productVariantId(),
              itemReq.quantity(),
              new Money(itemReq.price(), req.currency()),
              null,
              null));
    }

    order.setTotalManual(new Money(req.amountPaid(), req.currency()));

    // Fast-track state machine
    order.confirm();
    order.pay();
    order.completeImmediate();

    final Order saved = orderRepository.save(order);

    // 1. Audit
    auditPort.log(
        saved.getId(),
        tenantId,
        null,
        OrderState.Completed,
        OrderAuditActions.ORDER_COMPLETED,
        actor,
        "POS Sale");

    // 2. Saga state (immediate completion)
    sagaStatePort.upsert(saved.getId(), SagaStep.COMPLETE, "DONE");

    // 3. Outbox Events
    try {
      // Immediate stock deduction event
      var shippedItems =
          saved.getItems().stream()
              .map(i -> new OrderShippedEvent.Item(i.getProductVariantId(), i.getQuantity()))
              .toList();
      var shippedEvent =
          new OrderShippedEvent(
              saved.getId(), tenantId, saved.getCustomerId(), "POS-HANDOVER", shippedItems);
      outboxPort.publish(
          tenantId,
          saved.getId(),
          "OrderShippedEvent",
          objectMapper.writeValueAsString(shippedEvent));

      // Loyalty points event
      int points = saved.getTotal() != null ? saved.getTotal().getAmount().intValue() : 0;
      var completedEvent =
          new OrderCompletedEvent(tenantId, saved.getId(), saved.getCustomerId(), points);
      outboxPort.publish(
          tenantId,
          saved.getId(),
          "OrderCompletedEvent",
          objectMapper.writeValueAsString(completedEvent));

    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize POS order events", e);
    }

    metrics.counter("order.pos.created", "tenant", tenantId).increment();
    log.info(
        "POS Order id={} code={} completed for customer {}",
        saved.getId(),
        saved.getCode(),
        req.customerId());

    return toResponse(saved);
  }

  private OrderResponse toResponse(Order order) {
    List<OrderItemResponse> items =
        order.getItems().stream()
            .map(
                i ->
                    new OrderItemResponse(
                        i.getId(),
                        i.getProductVariantId(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getStartAt(),
                        i.getEndAt()))
            .toList();

    return new OrderResponse(
        order.getId(),
        order.getCode(),
        order.getCustomerId(),
        order.getState().name(),
        items,
        order.getSubTotal(),
        order.getTotal(),
        order.getShippingAddress(),
        order.getTrackingNumber(),
        order.getVoucherCode(),
        order.getDiscount(),
        order.getCreatedAt(),
        order.getUpdatedAt());
  }
}
