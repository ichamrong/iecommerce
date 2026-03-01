package com.chamrong.iecommerce.order.domain.ports;

import com.chamrong.iecommerce.order.domain.OrderOutboxEvent;
import java.util.List;

/** Port for order outbox persistence (transactional outbox pattern). */
public interface OrderOutboxPort {

  /**
   * Persists a new outbox event to be relayed (same transaction as aggregate change).
   *
   * @param tenantId tenant id
   * @param aggregateId order id
   * @param eventType event type
   * @param payload JSON payload
   */
  void publish(String tenantId, Long aggregateId, String eventType, String payload);

  /**
   * Loads pending events for relay, ordered by createdAt ASC.
   *
   * @param limit max events to return
   * @return pending events
   */
  List<OrderOutboxEvent> findPending(int limit);

  /**
   * Saves an existing event (e.g. after status update on relay).
   *
   * @param event event to save
   */
  void save(OrderOutboxEvent event);
}
