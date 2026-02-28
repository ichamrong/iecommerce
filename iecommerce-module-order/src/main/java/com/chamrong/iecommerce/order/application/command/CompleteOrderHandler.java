package com.chamrong.iecommerce.order.application.command;

import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.event.OrderCompletedEvent;
import com.chamrong.iecommerce.order.application.dto.OrderResponse;
import com.chamrong.iecommerce.order.application.dto.OrderResponse.OrderItemResponse;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderAuditActions;
import com.chamrong.iecommerce.order.domain.OrderState;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import com.chamrong.iecommerce.order.domain.ports.OrderOutboxPort;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import com.chamrong.iecommerce.order.domain.ports.OrderSagaStatePort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: Complete an order. Transitions state, publishes OrderCompletedEvent for loyalty points,
 * and marks saga as DONE.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteOrderHandler {

  private final OrderRepositoryPort orderRepository;
  private final OrderAuditPort auditPort;
  private final OrderOutboxPort outboxPort;
  private final OrderSagaStatePort sagaStatePort;
  private final ObjectMapper objectMapper;
  private final MeterRegistry metrics;

  @Transactional
  public OrderResponse handle(Long orderId, String actor) {
    Objects.requireNonNull(orderId, "orderId must not be null");

    final String tenantId = TenantContext.requireTenantId();
    final Order order =
        orderRepository
            .findByIdForUpdate(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

    if (!order.getTenantId().equals(tenantId)) {
      throw new org.springframework.security.access.AccessDeniedException("Access denied");
    }

    OrderState prev = order.getState();
    order.complete();

    final Order saved = orderRepository.save(order);

    // 1. Audit
    int points = saved.getTotal() != null ? saved.getTotal().getAmount().intValue() : 0;
    auditPort.log(
        saved.getId(),
        tenantId,
        prev,
        OrderState.Completed,
        OrderAuditActions.ORDER_COMPLETED,
        actor,
        "points=" + points);

    // 2. Saga state completion
    sagaStatePort.upsert(
        saved.getId(), com.chamrong.iecommerce.order.domain.saga.SagaStep.COMPLETE, "DONE");

    // 3. Outbox Event (Loyalty points, statistics, etc.)
    try {
      var event = new OrderCompletedEvent(tenantId, saved.getId(), saved.getCustomerId(), points);
      String payload = objectMapper.writeValueAsString(event);
      outboxPort.publish(tenantId, saved.getId(), "OrderCompletedEvent", payload);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize OrderCompletedEvent", e);
    }

    metrics.counter("order.completed", "tenant", tenantId).increment();
    log.info("Order completed id={} points={} tenantId={}", saved.getId(), points, tenantId);

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
