package com.chamrong.iecommerce.booking.domain.model;

/**
 * Lifecycle status of a booking. State machine: PENDING → ACCEPTED → CHECKIN → CHECKOUT, or CANCEL
 * from allowed states.
 *
 * <p>Values match persisted enum in booking table.
 */
public enum BookingStatus {
  /** Request received, awaiting host confirmation. */
  PENDING,

  /** Confirmed by host; ready for stay/appointment. */
  ACCEPTED,

  /** Guest is currently staying (accommodation) or in-session (appointment). */
  CHECKIN,

  /** Stay/session completed. */
  CHECKOUT,

  /** Cancelled; refund logic applies per policy. */
  CANCEL,

  /** Host rejected. */
  REJECTED
}
