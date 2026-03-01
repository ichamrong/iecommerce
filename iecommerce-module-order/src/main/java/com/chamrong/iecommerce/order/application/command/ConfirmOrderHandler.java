package com.chamrong.iecommerce.order.application.command;

import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.event.OrderConfirmedEvent;
import com.chamrong.iecommerce.order.application.dto.OrderResponse;
import com.chamrong.iecommerce.order.application.dto.OrderResponse.OrderItemResponse;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderAuditActions;
import com.chamrong.iecommerce.order.domain.OrderState;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import com.chamrong.iecommerce.order.domain.ports.OrderIdempotencyPort;
import com.chamrong.iecommerce.order.domain.ports.OrderOutboxPort;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import com.chamrong.iecommerce.order.domain.ports.OrderSagaStatePort;
import com.chamrong.iecommerce.order.domain.saga.SagaStep;
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
 * Use case: Confirm a draft order. Transitions state, publishes outbox event, and initializes saga
 * state. Protected by idempotency.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmOrderHandler {

  private final OrderRepositoryPort orderRepository;
  private final OrderAuditPort auditPort;
  private final OrderOutboxPort outboxPort;
  private final OrderIdempotencyPort idempotency;
  private final OrderSagaStatePort sagaStatePort;
  private final ObjectMapper objectMapper;
  private final MeterRegistry metrics;

  @Transactional
  public OrderResponse handle(Long orderId, String idempotencyKey, String actor) {
    Objects.requireNonNull(orderId, "orderId must not be null");

    final String tenantId = TenantContext.requireTenantId();

    // 1. Idempotency Check
    var existingResult = idempotency.check("CONFIRM_ORDER", idempotencyKey);
    if (existingResult.isPresent()) {
      log.info(
          "Duplicate confirm request detected for order {} and idempotencyKey {}. Returning cached"
              + " state.",
          orderId,
          idempotencyKey);
      return orderRepository
          .findById(orderId)
          .map(this::toResponse)
          .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    // 2. Load aggregate with pessimistic lock
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
    order.confirm();

    // 4. Persistence
    final Order saved = orderRepository.save(order);

    // 5. Audit
    auditPort.log(
        saved.getId(),
        tenantId,
        prev,
        OrderState.Confirmed,
        OrderAuditActions.ORDER_CONFIRMED,
        actor,
        "items=" + saved.getItems().size());

    // 6. Saga State Initialisation
    sagaStatePort.upsert(saved.getId(), SagaStep.RESERVE_INVENTORY, "RUNNING");

    // 7. Outbox Event
    try {
      var eventItems =
          saved.getItems().stream()
              .map(i -> new OrderConfirmedEvent.Item(i.getProductVariantId(), i.getQuantity()))
              .toList();
      var event =
          new OrderConfirmedEvent(saved.getId(), tenantId, saved.getCustomerId(), eventItems);
      String payload = objectMapper.writeValueAsString(event);
      outboxPort.publish(tenantId, saved.getId(), "OrderConfirmedEvent", payload);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize OrderConfirmedEvent", e);
    }

    // 8. Idempotency Record
    idempotency.record("CONFIRM_ORDER", idempotencyKey, "");

    metrics.counter("order.confirmed", "tenant", tenantId).increment();
    log.info("Order confirmed id={} code={} tenantId={}", saved.getId(), saved.getCode(), tenantId);

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
