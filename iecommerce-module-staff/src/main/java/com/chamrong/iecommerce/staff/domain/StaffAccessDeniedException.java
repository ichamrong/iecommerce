package com.chamrong.iecommerce.staff.domain;

/**
 * Domain-level exception indicating that a staff-related operation is not allowed.
 *
 * <p>Typical causes include cross-tenant access or attempting to act on a staff member outside the
 * caller's permitted scope.
 */
public class StaffAccessDeniedException extends RuntimeException {

  public StaffAccessDeniedException(String message) {
    super(message);
  }
}
