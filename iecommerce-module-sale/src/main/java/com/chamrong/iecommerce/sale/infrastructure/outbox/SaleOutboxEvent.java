package com.chamrong.iecommerce.sale.infrastructure.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sales_outbox_events")
@Getter
@Setter
@NoArgsConstructor
public class SaleOutboxEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OutboxStatus status = OutboxStatus.PENDING;

  @Column(name = "aggregate_id")
  private Long aggregateId;

  @Column(name = "retry_count", nullable = false)
  private int retryCount = 0;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "processed_at")
  private Instant processedAt;

  @Column(name = "next_attempt_at")
  private Instant nextAttemptAt;

  @Column(name = "trace_id")
  private String traceId;

  public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED
  }

  public SaleOutboxEvent(
      String tenantId, String eventType, String payload, Long aggregateId, String traceId) {
    this.tenantId = tenantId;
    this.eventType = eventType;
    this.payload = payload;
    this.aggregateId = aggregateId;
    this.traceId = traceId;
  }
}
