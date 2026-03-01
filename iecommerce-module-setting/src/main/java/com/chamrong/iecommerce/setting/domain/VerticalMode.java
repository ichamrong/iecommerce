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
          "order",
          "catalog",
          "inventory",
          "customer",
          "promotion",
          "payment",
          "invoice",
          "report",
          "setting")),

  POS(
      Set.of(
          "sale",
          "order",
          "catalog",
          "inventory",
          "customer",
          "promotion",
          "payment",
          "invoice",
          "report",
          "setting")),

  ACCOMMODATION(
      Set.of(
          "booking",
          "catalog",
          "inventory",
          "customer",
          "payment",
          "invoice",
          "report",
          "setting")),

  HYBRID(
      Set.of(
          "order",
          "sale",
          "booking",
          "catalog",
          "inventory",
          "customer",
          "promotion",
          "payment",
          "invoice",
          "report",
          "setting"));

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
