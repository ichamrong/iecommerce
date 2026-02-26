package com.chamrong.iecommerce.booking.domain;

/** Lifecycle status of a booking. */
public enum BookingStatus {
  /** Request received, payment authorized/escrowed, waiting for host confirmation. */
  PENDING,

  /** Host rejected the booking, triggering a refund. */
  REJECTED,

  /** Confirmed by the host, ready for stay. */
  ACCEPTED,

  /** Guest is currently staying (triggers payout). */
  CHECKIN,

  /** Stay is completed (archive). */
  CHECKOUT,

  /** Cancelled post-booking (triggers refund logic). */
  CANCEL
}
