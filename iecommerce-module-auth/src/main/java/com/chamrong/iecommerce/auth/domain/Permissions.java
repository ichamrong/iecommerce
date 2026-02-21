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

  // Audit management
  public static final String AUDIT_READ = "audit:read";

  // Self-service
  public static final String PROFILE_READ = "profile:read";

  // SpEL expressions for @PreAuthorize
  public static final String HAS_USER_READ = "hasAuthority('" + USER_READ + "')";
  public static final String HAS_USER_CREATE = "hasAuthority('" + USER_CREATE + "')";
  public static final String HAS_USER_DISABLE = "hasAuthority('" + USER_DISABLE + "')";
  public static final String HAS_TENANT_CREATE = "hasAuthority('" + TENANT_CREATE + "')";
  public static final String HAS_STAFF_MANAGE = "hasAuthority('" + STAFF_MANAGE + "')";
  public static final String HAS_PROFILE_READ = "hasAuthority('" + PROFILE_READ + "')";
  public static final String HAS_AUDIT_READ = "hasAuthority('" + AUDIT_READ + "')";

  private Permissions() {}
}
