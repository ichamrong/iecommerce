package com.chamrong.iecommerce.order.domain;

import java.util.Optional;

public interface OrderRepository {
  Optional<Order> findById(Long id);

  Optional<Order> findByCode(String code);

  Order save(Order order);
}
