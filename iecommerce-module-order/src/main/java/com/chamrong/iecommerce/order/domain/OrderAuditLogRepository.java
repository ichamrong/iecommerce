package com.chamrong.iecommerce.order.domain;

import java.util.List;

public interface OrderAuditLogRepository {
  void save(OrderAuditLog log);

  List<OrderAuditLog> findByOrderId(Long orderId);
}
