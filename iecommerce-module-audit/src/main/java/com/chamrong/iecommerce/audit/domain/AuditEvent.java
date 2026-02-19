package com.chamrong.iecommerce.audit.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;

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

  @Column(nullable = false)
  private Instant timestamp = Instant.now();

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getMetadata() {
    return metadata;
  }

  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }
}
