package com.chamrong.iecommerce.setting.domain;

/**
 * Canonical module code constants used across the platform.
 *
 * <p>These identifiers are used in {@link VerticalMode} and {@link
 * com.chamrong.iecommerce.setting.application.TenantCapabilityService} to gate module access by
 * tenant vertical.
 */
public final class ModuleCodes {

  private ModuleCodes() {}

  public static final String ORDER = "order";
  public static final String CATALOG = "catalog";
  public static final String INVENTORY = "inventory";
  public static final String CUSTOMER = "customer";
  public static final String PROMOTION = "promotion";
  public static final String PAYMENT = "payment";
  public static final String INVOICE = "invoice";
  public static final String REPORT = "report";
  public static final String SETTING = "setting";
  public static final String SALE = "sale";
  public static final String BOOKING = "booking";
}
