package com.chamrong.iecommerce.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseTenantEntity extends BaseEntity {

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  // Protected setter — called only from factory methods and assignTenantId() style methods
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }
}
