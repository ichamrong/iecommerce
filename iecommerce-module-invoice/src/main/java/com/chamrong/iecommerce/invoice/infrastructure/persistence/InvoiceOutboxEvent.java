package com.chamrong.iecommerce.invoice.infrastructure.persistence;

import com.chamrong.iecommerce.common.outbox.BaseOutboxEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Outbox event entity for invoice domain events.
 *
 * <p>Extends {@link BaseOutboxEvent} with additional fields for backoff scheduling and tracing.
 */
@Entity
@Table(name = "invoice_outbox_event")
@Getter
@NoArgsConstructor
public class InvoiceOutboxEvent extends BaseOutboxEvent {

  /** The invoice ID associated with this event for ordering and auditing. */
  @Column(name = "aggregate_id")
  private Long aggregateId;

  /**
   * When the next relay attempt should be made. Used for exponential backoff. Null = retry
   * immediately.
   */
  @Column(name = "next_attempt_at")
  private Instant nextAttemptAt;

  /** Distributed trace ID for correlating events across services. */
  @Column(name = "trace_id", length = 64)
  private String traceId;

  /**
   * Factory: creates a PENDING outbox event.
   *
   * @param tenantId owning tenant
   * @param eventType e.g. "INVOICE_ISSUED"
   * @param payload JSON payload
   * @param aggregateId invoice ID
   */
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

  /**
   * Applies exponential backoff with jitter after a failed relay attempt.
   *
   * @param baseDelaySeconds base delay for first retry (e.g. 5)
   */
  public void applyBackoff(int baseDelaySeconds) {
    int retries = getRetryCount();
    // backoff = baseDelay * 2^retries + jitter (0-1s)
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
