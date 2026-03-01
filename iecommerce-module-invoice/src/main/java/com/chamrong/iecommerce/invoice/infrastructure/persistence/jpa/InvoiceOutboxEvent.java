package com.chamrong.iecommerce.invoice.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.common.outbox.BaseOutboxEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Outbox event entity for invoice domain events. Extends {@link BaseOutboxEvent} with additional
 * fields for backoff scheduling and tracing.
 */
@Entity
@Table(name = "invoice_outbox_event")
@Getter
@NoArgsConstructor
public class InvoiceOutboxEvent extends BaseOutboxEvent {

  @Column(name = "aggregate_id")
  private Long aggregateId;

  @Column(name = "next_attempt_at")
  private Instant nextAttemptAt;

  @Column(name = "trace_id", length = 64)
  private String traceId;

  public static InvoiceOutboxEvent pending(
      String tenantId, String eventType, String payload, Long aggregateId) {
    InvoiceOutboxEvent e = new InvoiceOutboxEvent();
    e.setTenantId(tenantId);
    e.setEventType(eventType);
    e.setPayload(payload);
    e.setStatus(Status.PENDING);
    e.setCreatedAt(Instant.now());
    e.aggregateId = aggregateId;
    e.nextAttemptAt = null;
    return e;
  }

  public void applyBackoff(int baseDelaySeconds) {
    int retries = getRetryCount();
    long backoffSeconds = (long) (baseDelaySeconds * Math.pow(2, retries));
    long jitterMs = (long) (Math.random() * 1000);
    this.nextAttemptAt = Instant.now().plusSeconds(backoffSeconds).plusMillis(jitterMs);
  }

  public void setNextAttemptAt(Instant nextAttemptAt) {
    this.nextAttemptAt = nextAttemptAt;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }
}
