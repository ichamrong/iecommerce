package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.OrderOutboxEvent;
import com.chamrong.iecommerce.order.domain.ports.OrderOutboxPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * Adapter for order outbox: publish events in the same transaction as aggregate change, and find
 * pending events for relay.
 */
@Component
@RequiredArgsConstructor
public class JpaOrderOutboxAdapter implements OrderOutboxPort {

  private final OutboxSpringDataRepository repository;

  @Override
  public void publish(String tenantId, Long aggregateId, String eventType, String payload) {
    OrderOutboxEvent event = OrderOutboxEvent.pending(tenantId, eventType, payload);
    event.updateAggregateId(aggregateId);
    repository.save(event);
  }

  @Override
  public List<OrderOutboxEvent> findPending(int limit) {
    return repository.findPendingOrdered(PageRequest.of(0, limit));
  }

  @Override
  public void save(OrderOutboxEvent event) {
    repository.save(event);
  }

  @Repository
  interface OutboxSpringDataRepository extends JpaRepository<OrderOutboxEvent, Long> {

    @Query("SELECT e FROM OrderOutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<OrderOutboxEvent> findPendingOrdered(org.springframework.data.domain.Pageable pageable);
  }
}
