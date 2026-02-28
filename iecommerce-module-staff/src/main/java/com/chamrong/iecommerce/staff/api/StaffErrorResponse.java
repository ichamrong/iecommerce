package com.chamrong.iecommerce.staff.api;

/**
 * Typed error response payload for all Staff API errors.
 *
 * @param message human-readable error message
 * @param code machine-readable error code (e.g. STAFF_CONFLICT, NOT_FOUND)
 */
public record StaffErrorResponse(String message, String code) {
  public static StaffErrorResponse of(String message, String code) {
    return new StaffErrorResponse(message, code);
  }
}
