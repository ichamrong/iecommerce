package com.chamrong.iecommerce.setting.domain;

import java.util.Set;

/**
 * Tenant vertical mode: which business type(s) are active. Used by {@link
 * com.chamrong.iecommerce.setting.application.TenantCapabilityService} to determine allowed
 * modules.
 */
public enum VerticalMode {
  ECOMMERCE(
      Set.of(
          ModuleCodes.ORDER,
          ModuleCodes.CATALOG,
          ModuleCodes.INVENTORY,
          ModuleCodes.CUSTOMER,
          ModuleCodes.PROMOTION,
          ModuleCodes.PAYMENT,
          ModuleCodes.INVOICE,
          ModuleCodes.REPORT,
          ModuleCodes.SETTING)),

  POS(
      Set.of(
          ModuleCodes.SALE,
          ModuleCodes.ORDER,
          ModuleCodes.CATALOG,
          ModuleCodes.INVENTORY,
          ModuleCodes.CUSTOMER,
          ModuleCodes.PROMOTION,
          ModuleCodes.PAYMENT,
          ModuleCodes.INVOICE,
          ModuleCodes.REPORT,
          ModuleCodes.SETTING)),

  ACCOMMODATION(
      Set.of(
          ModuleCodes.BOOKING,
          ModuleCodes.CATALOG,
          ModuleCodes.INVENTORY,
          ModuleCodes.CUSTOMER,
          ModuleCodes.PAYMENT,
          ModuleCodes.INVOICE,
          ModuleCodes.REPORT,
          ModuleCodes.SETTING)),

  HYBRID(
      Set.of(
          ModuleCodes.ORDER,
          ModuleCodes.SALE,
          ModuleCodes.BOOKING,
          ModuleCodes.CATALOG,
          ModuleCodes.INVENTORY,
          ModuleCodes.CUSTOMER,
          ModuleCodes.PROMOTION,
          ModuleCodes.PAYMENT,
          ModuleCodes.INVOICE,
          ModuleCodes.REPORT,
          ModuleCodes.SETTING));

  private final Set<String> allowedModules;

  VerticalMode(Set<String> allowedModules) {
    this.allowedModules = allowedModules;
  }

  public Set<String> getAllowedModules() {
    return allowedModules;
  }

  public boolean allowsModule(String module) {
    return allowedModules.contains(module != null ? module.toLowerCase() : null);
  }

  /** Parse from string; default ECOMMERCE if invalid or null. */
  public static VerticalMode from(String value) {
    if (value == null || value.isBlank()) {
      return ECOMMERCE;
    }
    try {
      return valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return ECOMMERCE;
    }
  }
}
