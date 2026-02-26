package com.chamrong.iecommerce.order.domain;

import java.util.List;

public interface OrderOutboxRepository {
  void save(OrderOutboxEvent event);

  List<OrderOutboxEvent> findPending(int limit);
}
