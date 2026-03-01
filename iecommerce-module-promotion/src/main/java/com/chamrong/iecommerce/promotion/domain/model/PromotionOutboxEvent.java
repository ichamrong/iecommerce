package com.chamrong.iecommerce.promotion.domain.model;

import com.chamrong.iecommerce.common.outbox.BaseOutboxEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

/** reliable messaging outbox event for Promotion module. */
@Getter
@Entity
@Table(name = "promotion_outbox_event")
public class PromotionOutboxEvent extends BaseOutboxEvent {

  @Column(name = "aggregate_id")
  private Long aggregateId;

  @Column(name = "next_attempt_at", nullable = false)
  private Instant nextAttemptAt = Instant.now();

  @Column(name = "trace_id", length = 64)
  private String traceId;

  public static PromotionOutboxEvent pending(
      String tenantId, String eventType, String payload, Long aggregateId) {
    PromotionOutboxEvent e = new PromotionOutboxEvent();
    e.setTenantId(tenantId);
    e.setEventType(eventType);
    e.setPayload(payload);
    e.setStatus(Status.PENDING);
    e.setCreatedAt(Instant.now());
    e.aggregateId = aggregateId;
    return e;
  }

  public void updateNextAttemptAt(Instant nextAttemptAt) {
    this.nextAttemptAt = nextAttemptAt;
  }

  // Manual getters
  public Long getAggregateId() {
    return aggregateId;
  }

  public Instant getNextAttemptAt() {
    return nextAttemptAt;
  }

  public String getTraceId() {
    return traceId;
  }
}
