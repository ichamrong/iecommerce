package com.chamrong.iecommerce.booking.domain.ports;

import com.chamrong.iecommerce.booking.domain.model.BookingStatus;
import java.time.Instant;

/** Search criteria for booking list (cursor-safe; included in filterHash). */
public record BookingSearchCriteria(
    Long customerId,
    Long resourceProductId,
    BookingStatus status,
    Instant dateFrom,
    Instant dateTo,
    String searchTerm) {

  public static BookingSearchCriteria empty() {
    return new BookingSearchCriteria(null, null, null, null, null, null);
  }
}
