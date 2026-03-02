package com.chamrong.iecommerce.staff.domain;

/**
 * Canonical audit action codes for staff-related changes.
 *
 * <p>Using centralized constants helps avoid typos and ensures consistent reporting.
 */
public final class StaffAuditActions {

  private StaffAuditActions() {}

  public static final String STAFF_CREATED = "STAFF_CREATED";
  public static final String STAFF_UPDATED = "STAFF_UPDATED";
  public static final String STAFF_TENANTS_UPDATED = "STAFF_TENANTS_UPDATED";
  public static final String STAFF_SUSPENDED = "STAFF_SUSPENDED";
  public static final String STAFF_REACTIVATED = "STAFF_REACTIVATED";
  public static final String STAFF_TERMINATED = "STAFF_TERMINATED";
}
