package com.chamrong.iecommerce.order.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
  Optional<Order> findById(Long id);

  Optional<Order> findByCode(String code);

  Order save(Order order);

  List<Order> findByTenantIdAndCreatedAtBetween(String tenantId, Instant start, Instant end);
}
