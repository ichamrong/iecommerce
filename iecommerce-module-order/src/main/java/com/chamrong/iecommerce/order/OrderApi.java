package com.chamrong.iecommerce.order;

import com.chamrong.iecommerce.order.domain.Order;
import java.util.Optional;

/**
 * Public API of the Order module. Other modules must only depend on this interface, never on
 * internal classes like OrderService.
 */
public interface OrderApi {
  Order createOrder(String tenantId);

  com.chamrong.iecommerce.order.application.dto.OrderResponse createPosOrder(
      String tenantId, com.chamrong.iecommerce.order.application.dto.CreatePosOrderRequest req);

  Optional<Order> getOrder(Long id);
}
