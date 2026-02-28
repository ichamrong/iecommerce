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
import lombok.Setter;

/**
 * Entity to track idempotent write operations. Prevents re-execution of commands with the same
 * operation type and reference ID.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "order_idempotency")
public class OrderIdempotency {

  @EmbeddedId private IdempotencyId id = new IdempotencyId();

  @Column(name = "result_snapshot", columnDefinition = "TEXT")
  private String resultSnapshot;

  @Column(name = "recorded_at", nullable = false, updatable = false)
  private Instant recordedAt = Instant.now();

  public void setOperationType(String type) {
    this.id.operationType = type;
  }

  public String getOperationType() {
    return this.id.operationType;
  }

  public void setReferenceId(String ref) {
    this.id.referenceId = ref;
  }

  public String getReferenceId() {
    return this.id.referenceId;
  }

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
