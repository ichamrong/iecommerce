package com.chamrong.iecommerce.order.application.command;

import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.event.OrderShippedEvent;
import com.chamrong.iecommerce.order.application.dto.OrderResponse;
import com.chamrong.iecommerce.order.application.dto.OrderResponse.OrderItemResponse;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderAuditActions;
import com.chamrong.iecommerce.order.domain.OrderState;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import com.chamrong.iecommerce.order.domain.ports.OrderIdempotencyPort;
import com.chamrong.iecommerce.order.domain.ports.OrderOutboxPort;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
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
 * Use case: Ship an order with a tracking number. Transitions state and publishes OrderShippedEvent
 * for inventory deduction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShipOrderHandler {

  private final OrderRepositoryPort orderRepository;
  private final OrderAuditPort auditPort;
  private final OrderOutboxPort outboxPort;
  private final OrderIdempotencyPort idempotency;
  private final ObjectMapper objectMapper;
  private final MeterRegistry metrics;

  @Transactional
  public OrderResponse handle(Long orderId, String trackingNumber, String requestId, String actor) {
    Objects.requireNonNull(orderId, "orderId must not be null");

    final String tenantId = TenantContext.requireTenantId();

    // 1. Idempotency Check
    var existingResult = idempotency.check("SHIP_ORDER", requestId);
    if (existingResult.isPresent()) {
      return orderRepository
          .findById(orderId)
          .map(this::toResponse)
          .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    // 2. Load aggregate
    final Order order =
        orderRepository
            .findByIdForUpdate(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

    // Security check
    if (!order.getTenantId().equals(tenantId)) {
      throw new org.springframework.security.access.AccessDeniedException("Access denied");
    }

    OrderState prev = order.getState();

    // 3. Domain Logic
    order.ship(trackingNumber);

    // 4. Persistence
    final Order saved = orderRepository.save(order);

    // 5. Audit
    auditPort.log(
        saved.getId(),
        tenantId,
        prev,
        OrderState.Shipped,
        OrderAuditActions.ORDER_SHIPPED,
        actor,
        "tracking=" + trackingNumber);

    // 6. Outbox Event (Saga: trigger permanent stock deduction in inventory)
    try {
      var items =
          saved.getItems().stream()
              .map(i -> new OrderShippedEvent.Item(i.getProductVariantId(), i.getQuantity()))
              .toList();
      var event =
          new OrderShippedEvent(
              saved.getId(), tenantId, saved.getCustomerId(), trackingNumber, items);
      String payload = objectMapper.writeValueAsString(event);
      outboxPort.publish(tenantId, saved.getId(), "OrderShippedEvent", payload);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize OrderShippedEvent", e);
    }

    // 7. Idempotency Record
    idempotency.record("SHIP_ORDER", requestId, "");

    metrics.counter("order.shipped", "tenant", tenantId).increment();
    log.info(
        "Order shipped id={} tracking={} tenantId={}", saved.getId(), trackingNumber, tenantId);

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
