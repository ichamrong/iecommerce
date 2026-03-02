package com.chamrong.iecommerce.setting.domain;

/**
 * Canonical setting keys used within the settings module and by other bounded contexts.
 *
 * <p>Helps avoid typos and scattered string literals when reading/writing important settings.
 */
public final class SettingKeys {

  private SettingKeys() {}

  /**
   * Tenant vertical mode, used by {@link
   * com.chamrong.iecommerce.setting.application.TenantCapabilityService}.
   */
  public static final String VERTICAL_MODE = "vertical_mode";

  // Quota keys (examples; additional keys can be added as they are standardized)
  public static final String QUOTA_MAX_PRODUCTS = "quota.max_products";
  public static final String QUOTA_MAX_STAFF = "quota.max_staff";
  public static final String QUOTA_MAX_ASSETS = "quota.max_assets";
}
