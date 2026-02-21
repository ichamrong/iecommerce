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

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String action; // e.g., UPDATE_PRODUCT, PLACE_ORDER

  @Column(nullable = false)
  private String resourceType; // e.g., PRODUCT, ORDER

  private String resourceId;

  @Column(columnDefinition = "TEXT")
  private String metadata; // JSON representation of changes or extra info

  @Column(length = 45)
  private String ipAddress;

  @Column(length = 500)
  private String userAgent;

  @Column(nullable = false)
  private Instant timestamp = Instant.now();
}
