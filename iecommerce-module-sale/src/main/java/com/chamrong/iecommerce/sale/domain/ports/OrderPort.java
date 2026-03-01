package com.chamrong.iecommerce.sale.domain.ports;

import com.chamrong.iecommerce.common.Money;

public interface OrderPort {
  /**
   * Creates a sales order intent in the Order module. Implementation will likely be an async outbox
   * event or a synchronous client call.
   */
  void createSalesOrder(String tenantId, String customerId, Long quotationId, Money totalAmount);

  boolean exists(Long orderId, String tenantId);

  java.util.Optional<OrderItemInfo> getOrderItem(Long orderLineId, String tenantId);
}
