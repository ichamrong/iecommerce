package com.chamrong.iecommerce.booking;

import com.chamrong.iecommerce.booking.domain.Booking;
import java.util.Optional;

/**
 * Public API of the Booking module.
 *
 * <p>Other modules (e.g., Order, Notification) MUST only depend on this interface, never on
 * internal classes like BookingService.
 */
public interface BookingApi {
  Optional<Booking> getBooking(Long id);
}
