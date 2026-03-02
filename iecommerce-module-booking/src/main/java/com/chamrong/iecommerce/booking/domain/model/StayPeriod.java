package com.chamrong.iecommerce.booking.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Check-in and check-out window for accommodation bookings.
 *
 * @param checkIn check-in date/time (start of stay)
 * @param checkOut check-out date/time (end of stay)
 */
public record StayPeriod(Instant checkIn, Instant checkOut) {

  public StayPeriod {
    Objects.requireNonNull(checkIn, "checkIn");
    Objects.requireNonNull(checkOut, "checkOut");
    if (!checkOut.isAfter(checkIn)) {
      throw new IllegalArgumentException("checkOut must be after checkIn");
    }
  }

  /** Number of nights (for accommodation). */
  public long nights() {
    return java.time.Duration.between(checkIn, checkOut).toDays();
  }
}
