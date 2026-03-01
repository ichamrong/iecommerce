package com.chamrong.iecommerce.booking.infrastructure;

import com.chamrong.iecommerce.booking.domain.Booking;
import com.chamrong.iecommerce.booking.domain.BookingRepository;
import com.chamrong.iecommerce.booking.domain.BookingStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link BookingRepository} port. */
@Repository
public interface JpaBookingRepository extends JpaRepository<Booking, Long>, BookingRepository {

  @Override
  List<Booking> findByTenantIdAndCustomerId(String tenantId, Long customerId);

  @Override
  List<Booking> findByTenantIdAndResourceProductIdAndStatus(
      String tenantId, Long resourceProductId, BookingStatus status);

  @Override
  @Query(
      """
      SELECT b FROM Booking b
      WHERE b.tenantId = :tenantId
        AND b.resourceProductId = :pid
        AND b.status IN ('PENDING', 'ACCEPTED')
        AND b.startAt < :end
        AND b.endAt > :start
      """)
  List<Booking> findOverlappingBookings(
      @Param("tenantId") String tenantId,
      @Param("pid") Long resourceProductId,
      @Param("start") Instant start,
      @Param("end") Instant end);

  @Override
  @Query(
      """
      SELECT b FROM Booking b
      WHERE b.tenantId = :tenantId
        AND b.assignedStaffId = :sid
        AND b.status IN ('PENDING', 'ACCEPTED')
        AND b.startAt < :end
        AND b.endAt > :start
      """)
  List<Booking> findOverlappingStaffBookings(
      @Param("tenantId") String tenantId,
      @Param("sid") Long staffId,
      @Param("start") Instant start,
      @Param("end") Instant end);

  @Override
  List<Booking> findByStatusAndStartAtBetween(BookingStatus status, Instant start, Instant end);

  @Override
  List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, Instant createdAt);
}
