package com.chamrong.iecommerce.order.application.command;

import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.order.application.dto.OrderResponse;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderAuditActions;
import com.chamrong.iecommerce.order.domain.OrderState;
import com.chamrong.iecommerce.order.domain.ports.ClockPort;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Use case: Create a new draft order for the current tenant. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateOrderHandler {

  private final OrderRepositoryPort orderRepository;
  private final OrderAuditPort auditPort;
  private final ClockPort clock;
  private final MeterRegistry metrics;

  @Transactional
  public OrderResponse handle(String actor) {
    final String tenantId = TenantContext.requireTenantId();

    final Order order = new Order();
    // Use consistent naming for initial draft code
    order.assignCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    order.assignTenantId(tenantId);

    final Order saved = orderRepository.save(order);

    log.info(
        "Created draft order id={} code={} tenantId={}", saved.getId(), saved.getCode(), tenantId);

    // Audit the creation
    auditPort.log(
        saved.getId(),
        tenantId,
        null, // No previous state
        OrderState.AddingItems,
        OrderAuditActions.ORDER_CREATED,
        actor,
        "code=" + saved.getCode());

    metrics.counter("order.created", "tenant", tenantId).increment();

    return toResponse(saved);
  }

  private OrderResponse toResponse(Order order) {
    // Note: OrderItemResponse for a new order is always empty
    return new OrderResponse(
        order.getId(),
        order.getCode(),
        order.getCustomerId(),
        order.getState().name(),
        Collections.emptyList(),
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
