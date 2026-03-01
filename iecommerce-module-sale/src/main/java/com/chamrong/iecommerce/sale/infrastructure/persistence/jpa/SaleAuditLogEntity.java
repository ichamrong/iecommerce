package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sales_audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SaleAuditLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "actor_id", nullable = false)
  private String actorId;

  @Column(name = "action", nullable = false)
  private String action;

  @Column(name = "entity_name", nullable = false)
  private String entityName;

  @Column(name = "entity_id", nullable = false)
  private String entityId;

  @Column(name = "correlation_id", nullable = false)
  private String correlationId;

  @Column(name = "before_state_hash")
  private String beforeStateHash;

  @Column(name = "after_state_hash")
  private String afterStateHash;

  @Column(name = "prev_record_hash")
  private String prevRecordHash;

  @Column(name = "record_hash", nullable = false)
  private String recordHash;

  @Column(name = "timestamp", nullable = false)
  private Instant timestamp;

  public SaleAuditLogEntity(
      String tenantId,
      String actorId,
      String action,
      String entityName,
      String entityId,
      String correlationId,
      String beforeStateHash,
      String afterStateHash,
      String prevRecordHash,
      String recordHash) {
    this.tenantId = tenantId;
    this.actorId = actorId;
    this.action = action;
    this.entityName = entityName;
    this.entityId = entityId;
    this.correlationId = correlationId;
    this.beforeStateHash = beforeStateHash;
    this.afterStateHash = afterStateHash;
    this.prevRecordHash = prevRecordHash;
    this.recordHash = recordHash;
    this.timestamp = Instant.now();
  }
}
