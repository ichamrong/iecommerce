package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.OrderOutboxEvent;
import com.chamrong.iecommerce.order.domain.ports.OrderOutboxPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * Adapter for publishing events to the outbox table. Events are written within the same transaction
 * as the aggregate change.
 */
@Component
@RequiredArgsConstructor
public class JpaOrderOutboxAdapter implements OrderOutboxPort {

  private final OutboxSpringDataRepository repository;

  @Override
  public void publish(String tenantId, Long aggregateId, String eventType, String payload) {
    OrderOutboxEvent event = new OrderOutboxEvent();
    event.setTenantId(tenantId);
    event.setAggregateId(aggregateId);
    event.setEventType(eventType);
    event.setPayload(payload);
    // trace_id would normally be populated from MDC/Span context if tracing is enabled
    repository.save(event);
  }

  @Repository
  interface OutboxSpringDataRepository extends JpaRepository<OrderOutboxEvent, Long> {}
}
