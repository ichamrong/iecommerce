package com.chamrong.iecommerce.order.domain;

import com.chamrong.iecommerce.common.outbox.BaseOutboxEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Getter
@Entity
@Table(name = "order_outbox_event")
public class OrderOutboxEvent extends BaseOutboxEvent {

  @Column(name = "aggregate_id")
  private Long aggregateId;

  @Column(name = "next_attempt_at", nullable = false)
  private Instant nextAttemptAt = Instant.now();

  @Column(name = "trace_id", length = 64)
  private String traceId;

  public static OrderOutboxEvent pending(String tenantId, String eventType, String payload) {
    OrderOutboxEvent e = new OrderOutboxEvent();
    e.setTenantId(tenantId);
    e.setEventType(eventType);
    e.setPayload(payload);
    e.setStatus(Status.PENDING);
    e.setCreatedAt(Instant.now());
    return e;
  }

  public void updateNextAttemptAt(Instant nextAttemptAt) {
    this.nextAttemptAt = nextAttemptAt;
  }

  public void updateAggregateId(Long aggregateId) {
    this.aggregateId = aggregateId;
  }
}
