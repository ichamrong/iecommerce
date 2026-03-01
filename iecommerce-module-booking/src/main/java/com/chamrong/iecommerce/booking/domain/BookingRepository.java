package com.chamrong.iecommerce.booking.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {
  Booking save(Booking booking);

  Optional<Booking> findById(Long id);

  List<Booking> findByTenantIdAndCustomerId(String tenantId, Long customerId);

  List<Booking> findByTenantIdAndResourceProductIdAndStatus(
      String tenantId, Long resourceProductId, BookingStatus status);

  /** Returns all bookings for a resource that overlap with the given window. Tenant-scoped. */
  List<Booking> findOverlappingBookings(
      String tenantId, Long resourceProductId, Instant start, Instant end);

  /** Returns all bookings for a staff member that overlap with the given window. Tenant-scoped. */
  List<Booking> findOverlappingStaffBookings(
      String tenantId, Long staffId, Instant start, Instant end);

  List<Booking> findByStatusAndStartAtBetween(BookingStatus status, Instant start, Instant end);

  List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, Instant createdAt);
}
