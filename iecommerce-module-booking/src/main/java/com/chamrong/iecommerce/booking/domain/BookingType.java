package com.chamrong.iecommerce.booking.domain;

/** What type of booking this slot represents. */
public enum BookingType {
  /** Appointment-style (salon, clinic, consultant) — slot is measured in minutes. */
  APPOINTMENT,

  /** Nightly stay (hotel, villa) — slot spans whole calendar dates. */
  ACCOMMODATION
}
