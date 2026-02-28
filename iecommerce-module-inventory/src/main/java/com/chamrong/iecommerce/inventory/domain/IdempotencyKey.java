package com.chamrong.iecommerce.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;

/**
 * Idempotency record for inventory write operations.
 *
 * <p>Before executing any mutating operation (receive, adjust, reserve, commit, release), the
 * handler checks whether a record with {@code (tenant_id, operation_type, reference_id)} already
 * exists. If it does, the handler returns the cached result without re-executing the operation.
 *
 * <p>The DB unique constraint provides the last line of defense under concurrent retries — a race
 * between two identical requests will result in one succeeding and one receiving a constraint
 * violation, which the application layer converts to a pass-through.
 *
 * <p>Table: {@code inventory_idempotency_key}
 */
@Getter
@Entity
@Table(
    name = "inventory_idempotency_key",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_idempotency",
            columnNames = {"tenant_id", "operation_type", "reference_id"}))
public class IdempotencyKey {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, updatable = false, length = 100)
  private String tenantId;

  @Column(name = "operation_type", nullable = false, updatable = false, length = 100)
  private String operationType;

  @Column(name = "reference_id", nullable = false, updatable = false, length = 255)
  private String referenceId;

  /** JSON-encoded result snapshot, returned on duplicate requests. */
  @Column(name = "result_snapshot", updatable = false, columnDefinition = "TEXT")
  private String resultSnapshot;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /** Allows periodic cleanup of stale keys. Nullable = never expires. */
  @Column(name = "expires_at")
  private Instant expiresAt;

  private IdempotencyKey() {}

  public static IdempotencyKey of(
      String tenantId,
      String operationType,
      String referenceId,
      String resultSnapshot,
      Instant now,
      Instant expiresAt) {
    var k = new IdempotencyKey();
    k.tenantId = tenantId;
    k.operationType = operationType;
    k.referenceId = referenceId;
    k.resultSnapshot = resultSnapshot;
    k.createdAt = now;
    k.expiresAt = expiresAt;
    return k;
  }
}
