package com.chamrong.iecommerce.common;

import org.springframework.lang.Nullable;

public class TenantContext {

  private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

  public static void setCurrentTenant(String tenantId) {
    CURRENT_TENANT.set(tenantId);
  }

  @Nullable
  public static String getCurrentTenant() {
    return CURRENT_TENANT.get();
  }

  public static String requireTenantId() {
    String tenantId = getCurrentTenant();
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalStateException("No tenant context found");
    }
    return tenantId;
  }

  public static void clear() {
    CURRENT_TENANT.remove();
  }
}
