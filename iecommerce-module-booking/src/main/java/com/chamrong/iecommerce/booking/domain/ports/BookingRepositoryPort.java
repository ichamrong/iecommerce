package com.chamrong.iecommerce.booking.domain.ports;

import com.chamrong.iecommerce.booking.domain.model.Booking;
import com.chamrong.iecommerce.booking.domain.model.BookingStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Port for booking persistence. All queries MUST be tenant-scoped.
 */
public interface BookingRepositoryPort {

  Booking save(Booking booking);

  Optional<Booking> findById(Long id);

  Optional<Booking> findByIdAndTenantId(Long id, String tenantId);

  List<Booking> findByCustomerId(String tenantId, Long customerId);

  List<Booking> findByResourceProductIdAndStatus(
      String tenantId, Long resourceProductId, BookingStatus status);

  List<Booking> findOverlappingBookings(
      String tenantId, Long resourceProductId, Instant start, Instant end);

  List<Booking> findOverlappingStaffBookings(
      String tenantId, Long staffId, Instant start, Instant end);

  /** Keyset pagination: first page. */
  List<Booking> findFirstPage(String tenantId, BookingSearchCriteria criteria, int limitPlusOne);

  /** Keyset pagination: next page. */
  List<Booking> findNextPage(
      String tenantId,
      BookingSearchCriteria criteria,
      Instant cursorCreatedAt,
      Long cursorId,
      int limitPlusOne);
}
