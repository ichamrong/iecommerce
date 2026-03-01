package com.chamrong.iecommerce.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity to track idempotent write operations. Prevents re-execution of commands with the same
 * operation type and reference ID.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "order_idempotency")
public class OrderIdempotency {

  @EmbeddedId private IdempotencyId id = new IdempotencyId();

  @Column(name = "result_snapshot", columnDefinition = "TEXT")
  private String resultSnapshot;

  @Column(name = "recorded_at", nullable = false, updatable = false)
  private Instant recordedAt = Instant.now();

  public static OrderIdempotency of(
      String operationType, String referenceId, String resultSnapshot) {
    var e = new OrderIdempotency();
    e.id.operationType = operationType;
    e.id.referenceId = referenceId;
    e.resultSnapshot = resultSnapshot;
    return e;
  }

  public String getOperationType() {
    return this.id.operationType;
  }

  public String getReferenceId() {
    return this.id.referenceId;
  }

  /**
   * @Data is acceptable on @Embeddable composite keys — equals/hashCode on the key identity fields
   * is required and correct here.
   */
  @Data
  @Embeddable
  @NoArgsConstructor
  @AllArgsConstructor
  public static class IdempotencyId implements Serializable {
    @Column(name = "operation_type", length = 100)
    private String operationType;

    @Column(name = "reference_id", length = 255)
    private String referenceId;
  }
}
