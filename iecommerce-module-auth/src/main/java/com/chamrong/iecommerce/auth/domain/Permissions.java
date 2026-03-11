package com.chamrong.iecommerce.auth.domain;

/** Fine-grained permission name constants used in {@code @PreAuthorize}. */
public final class Permissions {

  // User management
  public static final String USER_READ = "user:read";
  public static final String USER_CREATE = "user:create";
  public static final String USER_DISABLE = "user:disable";

  // Tenant management
  public static final String TENANT_CREATE = "tenant:create";

  // Staff management
  public static final String STAFF_MANAGE = "staff:manage";

  // Audit management (AUDIT_READ = AUDIT_VIEW for read; AUDIT_WRITE for privileged write)
  public static final String AUDIT_READ = "audit:read";
  public static final String AUDIT_WRITE = "audit:write";

  // Sale & Invoice management
  public static final String SALE_READ = "sales:read";
  public static final String SALE_MANAGE = "sales:manage";
  public static final String INVOICE_READ = "invoices:read";
  public static final String INVOICE_MANAGE = "invoices:manage";

  // Self-service
  public static final String PROFILE_READ = "profile:read";

  // eKYC / Merchant approvals
  public static final String EKYC_READ = "ekyc:read";
  public static final String EKYC_REVIEW = "ekyc:review";

  // Helpdesk
  public static final String HELPDESK_READ = "helpdesk:read";
  public static final String HELPDESK_REPLY = "helpdesk:reply";

  // Finance / Ledgers
  public static final String FINANCE_MANAGE = "finance:manage";

  // SpEL expressions for @PreAuthorize
  public static final String HAS_USER_READ = "hasAuthority('" + USER_READ + "')";
  public static final String HAS_USER_CREATE = "hasAuthority('" + USER_CREATE + "')";
  public static final String HAS_USER_DISABLE = "hasAuthority('" + USER_DISABLE + "')";
  public static final String HAS_TENANT_CREATE = "hasAuthority('" + TENANT_CREATE + "')";
  public static final String HAS_STAFF_MANAGE = "hasAuthority('" + STAFF_MANAGE + "')";
  public static final String HAS_PROFILE_READ = "hasAuthority('" + PROFILE_READ + "')";
  public static final String HAS_AUDIT_READ = "hasAuthority('" + AUDIT_READ + "')";
  public static final String HAS_AUDIT_WRITE = "hasAuthority('" + AUDIT_WRITE + "')";
  public static final String HAS_SALE_READ = "hasAuthority('" + SALE_READ + "')";
  public static final String HAS_SALE_MANAGE = "hasAuthority('" + SALE_MANAGE + "')";
  public static final String HAS_INVOICE_READ = "hasAuthority('" + INVOICE_READ + "')";
  public static final String HAS_INVOICE_MANAGE = "hasAuthority('" + INVOICE_MANAGE + "')";
  public static final String HAS_EKYC_READ = "hasAuthority('" + EKYC_READ + "')";
  public static final String HAS_EKYC_REVIEW = "hasAuthority('" + EKYC_REVIEW + "')";
  public static final String HAS_HELPDESK_READ = "hasAuthority('" + HELPDESK_READ + "')";
  public static final String HAS_HELPDESK_REPLY = "hasAuthority('" + HELPDESK_REPLY + "')";
  public static final String HAS_FINANCE_MANAGE = "hasAuthority('" + FINANCE_MANAGE + "')";

  private Permissions() {}
}
