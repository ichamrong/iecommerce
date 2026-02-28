package com.chamrong.iecommerce.order.application.command;

import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.order.application.dto.OrderResponse;
import com.chamrong.iecommerce.order.application.dto.OrderResponse.OrderItemResponse;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderAuditActions;
import com.chamrong.iecommerce.order.domain.OrderState;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Use case: Internal fulfillment step — Pick items for order. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PickOrderHandler {

  private final OrderRepositoryPort orderRepository;
  private final OrderAuditPort auditPort;
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
    order.pick();

    final Order saved = orderRepository.save(order);
    auditPort.log(
        saved.getId(),
        tenantId,
        prev,
        OrderState.Picking,
        OrderAuditActions.ORDER_PICKING,
        actor,
        null);
    metrics.counter("order.picking", "tenant", tenantId).increment();

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
