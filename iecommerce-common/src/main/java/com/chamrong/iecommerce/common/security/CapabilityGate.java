package com.chamrong.iecommerce.common.security;

/**
 * Port for enforcing tenant capabilities (vertical mode, enabled modules, quotas, feature flags).
 * Implementations load tenant settings and throw {@link CapabilityDeniedException} when access is
 * not allowed.
 */
public interface CapabilityGate {

  /**
   * Ensures the tenant is allowed to use the given module (e.g. "sale", "booking"). Throws {@link
   * CapabilityDeniedException} with {@link CapabilityDeniedException#MODULE_DISABLED} if not
   * allowed.
   *
   * @param tenantId current tenant id (from TenantContext)
   * @param module module key (e.g. "sale", "order", "booking")
   * @throws CapabilityDeniedException if module is disabled for this tenant
   */
  void requireModule(String tenantId, String module);
}
