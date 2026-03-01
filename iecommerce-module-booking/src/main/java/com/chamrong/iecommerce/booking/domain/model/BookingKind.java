package com.chamrong.iecommerce.booking.domain.model;

/**
 * Type of booking: accommodation (night-based) or appointment (time-slot based).
 *
 * <p>Enforces rules per kind via policy and state machine.
 */
public enum BookingKind {
  /** Night-based: check-in/check-out dates, room type, occupancy. */
  ACCOMMODATION,

  /** Time-slot based: startAt/endAt, resourceId (staff/room). */
  APPOINTMENT
}
