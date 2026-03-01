package com.chamrong.iecommerce.common.domain;

import lombok.Getter;

/** Persistence-free base for tenant-scoped domain aggregates. Use in domain layer only. */
@Getter
public abstract class BaseDomainTenantEntity extends BaseDomainEntity {

  private String tenantId;

  /** Write-once; for construction or reconstitution from persistence. */
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }
}
