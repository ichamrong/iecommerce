package com.chamrong.iecommerce.order;

import com.chamrong.iecommerce.order.domain.Order;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Public API of the Order module. Other modules must only depend on this interface, never on
 * internal classes like OrderService or domain types for cross-module use.
 */
public interface OrderApi {
  Order createOrder(String tenantId);

  com.chamrong.iecommerce.order.application.dto.OrderResponse createPosOrder(
      String tenantId, com.chamrong.iecommerce.order.application.dto.CreatePosOrderRequest req);

  Optional<Order> getOrder(Long id);

  /**
   * Returns orders in a time range for reconciliation reports. Only orders in reconcilable states
   * (excludes AddingItems and Cancelled) are returned. Used by the report module.
   */
  List<OrderReconciliationItem> findOrdersForReconciliation(
      String tenantId, Instant start, Instant end);
}
