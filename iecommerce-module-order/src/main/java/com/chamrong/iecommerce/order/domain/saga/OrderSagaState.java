package com.chamrong.iecommerce.order.domain.saga;

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

/**
 * Tracks saga progress per order for observability and safe re-processing.
 *
 * <p>One row per order. This is not the source of truth for order state — the {@link
 * com.chamrong.iecommerce.order.domain.Order} entity is. This table exists purely to make the
 * distributed choreography saga visible and debuggable without adding complexity to the core
 * aggregate.
 *
 * <p>Design choice — choreography kept (vs. orchestration): The existing event-listener pattern
 * already works. Adding a saga state table gives observability (which step failed? how many
 * retries?) without the operational overhead of a full saga orchestrator.
 *
 * <p>Status values:
 *
 * <ul>
 *   <li>{@code RUNNING} — normal forward progress
 *   <li>{@code COMPENSATING} — a compensation step is in progress
 *   <li>{@code DONE} — saga completed successfully
 *   <li>{@code FAILED} — compensation exhausted
 * </ul>
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "order_saga_state")
public class OrderSagaState {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false, unique = true)
  private Long orderId;

  @Enumerated(EnumType.STRING)
  @Column(name = "current_step", nullable = false, length = 50)
  private SagaStep currentStep;

  /** RUNNING | COMPENSATING | DONE | FAILED */
  @Column(nullable = false, length = 20)
  private String status;

  /** Correlation ID thread — e.g. payment provider reference. */
  @Column(name = "correlation_id", length = 255)
  private String correlationId;

  @Column(name = "retry_count", nullable = false)
  private int retryCount = 0;

  /** Last reason for failure or compensation — for operations team. */
  @Column(name = "last_reason", length = 1000)
  private String lastReason;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  // ── Factory + mutations ───────────────────────────────────────────────────

  public static OrderSagaState start(Long orderId, SagaStep step) {
    var s = new OrderSagaState();
    s.orderId = orderId;
    s.currentStep = step;
    s.status = "RUNNING";
    s.updatedAt = Instant.now();
    return s;
  }

  public void advance(SagaStep step) {
    this.currentStep = step;
    this.status = "RUNNING";
    this.updatedAt = Instant.now();
  }

  public void complete() {
    this.currentStep = SagaStep.COMPLETE;
    this.status = "DONE";
    this.updatedAt = Instant.now();
  }

  public void beginCompensation(String reason) {
    this.status = "COMPENSATING";
    this.lastReason = reason;
    this.updatedAt = Instant.now();
  }

  public void fail(String reason) {
    this.status = "FAILED";
    this.lastReason = reason;
    this.updatedAt = Instant.now();
  }

  public void incrementRetry() {
    this.retryCount++;
    this.updatedAt = Instant.now();
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
    this.updatedAt = Instant.now();
  }
}
