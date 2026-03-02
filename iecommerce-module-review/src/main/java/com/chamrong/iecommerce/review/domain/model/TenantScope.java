package com.chamrong.iecommerce.review.domain.model;

/** Value object representing the tenant scope of a review aggregate. */
public record TenantScope(String tenantId) {

  public TenantScope {
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("tenantId must not be blank");
    }
  }

  public void assertSameTenant(String otherTenantId) {
    if (otherTenantId == null || !tenantId.equals(otherTenantId)) {
      throw new IllegalArgumentException("Cross-tenant access is not allowed");
    }
  }
}
