package com.chamrong.iecommerce.order.application.command;

import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.security.TenantGuard;
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

/** Use case: Transition order to Delivered status. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliverOrderHandler {

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

    TenantGuard.requireSameTenant(order.getTenantId(), tenantId);

    OrderState prev = order.getState();
    order.deliver();

    final Order saved = orderRepository.save(order);
    auditPort.log(
        saved.getId(),
        tenantId,
        prev,
        OrderState.Delivered,
        OrderAuditActions.ORDER_DELIVERED,
        actor,
        null);
    metrics.counter("order.delivered", "tenant", tenantId).increment();

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
