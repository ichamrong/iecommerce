package com.chamrong.iecommerce.common;

import org.springframework.lang.Nullable;

public class TenantContext {

  private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

  /**
   * When set as current tenant, downstream code may treat it as "platform admin, no tenant scope"
   * and list/query across all tenants. Set by {@link
   * com.chamrong.iecommerce.auth.infrastructure.security.TenantContextFilter} when JWT has no
   * tenant_id and user has ROLE_PLATFORM_ADMIN.
   */
  public static final String PLATFORM_ADMIN_SENTINEL = "__PLATFORM_ADMIN__";

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
