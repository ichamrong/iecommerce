package com.chamrong.iecommerce.audit.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "audit_log")
public class AuditEvent extends BaseTenantEntity {

  @Column(nullable = false, updatable = false)
  private String userId;

  @Column(nullable = false, updatable = false)
  private String action; // e.g., UPDATE_PRODUCT, PLACE_ORDER

  @Column(nullable = false, updatable = false)
  private String resourceType; // e.g., PRODUCT, ORDER

  @Column(updatable = false)
  private String resourceId;

  @Column(columnDefinition = "TEXT", updatable = false)
  private String metadata; // JSON representation of changes or extra info

  @Column(length = 45, updatable = false)
  private String ipAddress;

  @Column(length = 500, updatable = false)
  private String userAgent;

  @Column(nullable = false, updatable = false)
  private Instant timestamp = Instant.now();

  @PreUpdate
  public void onPreUpdate() {
    throw new UnsupportedOperationException("Audit events are immutable and cannot be modified.");
  }
}
