package com.chamrong.iecommerce.auth.domain;

/** Canonical role name constants used in {@code @PreAuthorize} and seed data. */
public final class Roles {

  public static final String ADMIN = "ADMIN";
  public static final String USER = "USER";

  // SpEL expressions for @PreAuthorize — avoids magic strings in annotations
  public static final String HAS_ROLE_ADMIN = "hasRole('" + ADMIN + "')";
  public static final String HAS_ROLE_USER = "hasRole('" + USER + "')";

  private Roles() {}
}
