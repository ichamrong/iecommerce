package com.chamrong.iecommerce.audit.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity for audit_event table. Append-only; no updates. Tenant-scoped.
 */
@Entity
@Table(name = "audit_event")
@Getter
@Setter
public class AuditEventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
  private String tenantId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "correlation_id", length = 255)
  private String correlationId;

  @Column(name = "actor_id", nullable = false, updatable = false, length = 255)
  private String actorId;

  @Column(name = "actor_type", nullable = false, updatable = false, length = 64)
  private String actorType;

  @Column(name = "actor_role", length = 128)
  private String actorRole;

  @Column(name = "event_type", nullable = false, updatable = false, length = 128)
  private String eventType;

  @Column(name = "outcome", nullable = false, updatable = false, length = 32)
  private String outcome;

  @Column(name = "severity", nullable = false, updatable = false, length = 32)
  private String severity;

  @Column(name = "target_type", nullable = false, updatable = false, length = 128)
  private String targetType;

  @Column(name = "target_id", length = 255)
  private String targetId;

  @Column(name = "source_module", length = 128)
  private String sourceModule;

  @Column(name = "source_endpoint", length = 255)
  private String sourceEndpoint;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @Column(name = "metadata_json", columnDefinition = "TEXT")
  private String metadataJson;

  @Column(name = "prev_hash", length = 64)
  private String prevHash;

  @Column(name = "hash", nullable = false, updatable = false, length = 64)
  private String hash;
}
