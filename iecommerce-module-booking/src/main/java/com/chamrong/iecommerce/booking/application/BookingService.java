package com.chamrong.iecommerce.booking.application;

import com.chamrong.iecommerce.booking.BookingApi;
import com.chamrong.iecommerce.booking.BookingCompletedEvent;
import com.chamrong.iecommerce.booking.BookingConfirmedEvent;
import com.chamrong.iecommerce.booking.application.dto.AvailabilityRuleRequest;
import com.chamrong.iecommerce.booking.application.dto.AvailabilityRuleResponse;
import com.chamrong.iecommerce.booking.application.dto.AvailableSlot;
import com.chamrong.iecommerce.booking.application.dto.BookingResponse;
import com.chamrong.iecommerce.booking.application.dto.CreateBookingRequest;
import com.chamrong.iecommerce.booking.domain.AvailabilityRule;
import com.chamrong.iecommerce.booking.domain.AvailabilityRuleRepository;
import com.chamrong.iecommerce.booking.domain.BlockedSlot;
import com.chamrong.iecommerce.booking.domain.Booking;
import com.chamrong.iecommerce.booking.domain.BookingRepository;
import com.chamrong.iecommerce.booking.domain.BookingStatus;
import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.security.TenantGuard;
import jakarta.persistence.EntityNotFoundException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService implements BookingApi {

  private final BookingRepository bookingRepository;
  private final AvailabilityRuleRepository availabilityRuleRepository;
  private final ApplicationEventPublisher eventPublisher;

  // ── Commands ───────────────────────────────────────────────────────────────

  @Transactional
  public BookingResponse createBooking(String tenantId, CreateBookingRequest req) {
    // Guard: check for overlapping active bookings (tenant-scoped)
    List<Booking> conflicts =
        bookingRepository.findOverlappingBookings(
            tenantId, req.resourceProductId(), req.startAt(), req.endAt());
    if (!conflicts.isEmpty()) {
      throw new IllegalStateException(
          "Resource is already booked during the requested time window ("
              + conflicts.size()
              + " conflict(s))");
    }

    // Guard: check for staff conflicts if assigned (tenant-scoped)
    if (req.assignedStaffId() != null) {
      List<Booking> staffConflicts =
          bookingRepository.findOverlappingStaffBookings(
              tenantId, req.assignedStaffId(), req.startAt(), req.endAt());
      if (!staffConflicts.isEmpty()) {
        throw new IllegalStateException("Staff member is already booked during this time");
      }
    }

    Booking booking =
        Booking.create(
            tenantId,
            req.customerId(),
            req.resourceProductId(),
            req.resourceVariantId(),
            req.assignedStaffId(),
            req.startAt(),
            req.endAt(),
            req.type(),
            req.customerNotes(),
            req.totalPriceAmount() != null
                ? new Money(req.totalPriceAmount(), req.totalPriceCurrency())
                : null);

    // Block the slot immediately to prevent race-condition double-booking
    BlockedSlot slot =
        BlockedSlot.forBooking(
            req.resourceProductId(), req.resourceVariantId(), req.startAt(), req.endAt());
    booking.getBlockedSlots().add(slot);

    Booking saved = bookingRepository.save(booking);
    log.info(
        "Booking created id={} resource={} [{} → {}]",
        saved.getId(),
        req.resourceProductId(),
        req.startAt(),
        req.endAt());
    return toResponse(saved);
  }

  @Transactional
  public BookingResponse accept(Long bookingId) {
    Booking booking = require(bookingId);
    booking.accept();
    Booking saved = bookingRepository.save(booking);

    eventPublisher.publishEvent(
        new BookingConfirmedEvent(
            saved.getTenantId(),
            saved.getId(),
            saved.getCustomerId(),
            saved.getResourceProductId(),
            saved.getResourceVariantId(),
            saved.getStartAt(),
            saved.getEndAt(),
            saved.getTotalPrice()));
    log.info("Booking accepted id={}", bookingId);
    return toResponse(saved);
  }

  @Transactional
  public BookingResponse reject(Long bookingId, String reason) {
    Booking booking = require(bookingId);
    booking.reject(reason);
    Booking saved = bookingRepository.save(booking);
    log.info("Booking rejected id={} reason={}", bookingId, reason);
    return toResponse(saved);
  }

  @Transactional
  public BookingResponse checkIn(Long bookingId) {
    Booking booking = require(bookingId);
    booking.checkIn();
    Booking saved = bookingRepository.save(booking);
    log.info("Booking checked-in id={}", bookingId);
    return toResponse(saved);
  }

  @Transactional
  public BookingResponse checkOut(Long bookingId) {
    Booking booking = require(bookingId);
    booking.checkOut();
    Booking saved = bookingRepository.save(booking);
    eventPublisher.publishEvent(
        new BookingCompletedEvent(
            saved.getTenantId(),
            saved.getId(),
            saved.getCustomerId(),
            saved.getResourceProductId()));
    log.info("Booking checked-out id={}", bookingId);
    return toResponse(saved);
  }

  @Transactional
  public BookingResponse cancel(Long bookingId, String reason) {
    Booking booking = require(bookingId);
    booking.cancel(reason);
    Booking saved = bookingRepository.save(booking);
    log.info("Booking cancelled id={} reason={}", bookingId, reason);
    return toResponse(saved);
  }

  // ── Queries ────────────────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public Optional<Booking> getBooking(Long id) {
    return bookingRepository.findById(id);
  }

  @Transactional(readOnly = true)
  public Optional<BookingResponse> findById(Long id) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    return bookingRepository
        .findById(id)
        .map(
            booking -> {
              TenantGuard.requireSameTenant(booking.getTenantId(), tenantId);
              return toResponse(booking);
            });
  }

  @Transactional(readOnly = true)
  public List<BookingResponse> getCustomerBookings(String tenantId, Long customerId) {
    return bookingRepository
        .findByTenantIdAndCustomerId(tenantId, customerId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<BookingResponse> getResourceAcceptedBookings(
      String tenantId, Long resourceProductId) {
    return bookingRepository
        .findByTenantIdAndResourceProductIdAndStatus(
            tenantId, resourceProductId, BookingStatus.ACCEPTED)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  // ── Availability engine ────────────────────────────────────────────────────

  /**
   * Returns available time slots for a resource on the given calendar date.
   *
   * <p>Algorithm:
   *
   * <ol>
   *   <li>Fetch all AvailabilityRules for resource + day-of-week
   *   <li>Generate slot candidates (open..close at slotDuration intervals)
   *   <li>For each candidate, check for overlapping confirmed/pending bookings
   *   <li>Return all slots with their availability flag
   * </ol>
   */
  /**
   * Returns available time slots for a resource (and optionally a staff member) on the given date.
   */
  @Transactional(readOnly = true)
  public List<AvailableSlot> getAvailableSlots(
      String tenantId, Long resourceProductId, Long staffId, LocalDate date) {
    DayOfWeek dow = date.getDayOfWeek();

    List<AvailabilityRule> rules;
    if (staffId != null) {
      rules = availabilityRuleRepository.findByStaffIdAndDayOfWeek(staffId, dow);
    } else {
      rules =
          availabilityRuleRepository.findByResourceProductIdAndDayOfWeek(resourceProductId, dow);
    }

    List<AvailableSlot> slots = new ArrayList<>();
    for (AvailabilityRule rule : rules) {
      LocalTime cursor = rule.getOpenTime();
      while (cursor.plusMinutes(rule.getSlotDurationMinutes()).compareTo(rule.getCloseTime())
          <= 0) {
        Instant slotStart = date.atTime(cursor).toInstant(ZoneOffset.UTC);
        Instant slotEnd =
            date.atTime(cursor.plusMinutes(rule.getSlotDurationMinutes()))
                .toInstant(ZoneOffset.UTC);

        boolean hasConflict =
            !bookingRepository
                .findOverlappingBookings(tenantId, resourceProductId, slotStart, slotEnd)
                .isEmpty();
        if (!hasConflict && staffId != null) {
          hasConflict =
              !bookingRepository
                  .findOverlappingStaffBookings(tenantId, staffId, slotStart, slotEnd)
                  .isEmpty();
        }

        slots.add(new AvailableSlot(slotStart, slotEnd, !hasConflict));
        cursor = cursor.plusMinutes(rule.getSlotDurationMinutes());
      }
    }
    return slots;
  }

  // ── Availability rules ─────────────────────────────────────────────────────

  @Transactional
  public AvailabilityRuleResponse addRule(String tenantId, AvailabilityRuleRequest req) {
    AvailabilityRule rule =
        AvailabilityRule.of(
            tenantId,
            req.resourceProductId(),
            req.resourceVariantId(),
            req.staffId(),
            req.dayOfWeek(),
            req.openTime(),
            req.closeTime(),
            req.slotDurationMinutes());
    return toRuleResponse(availabilityRuleRepository.save(rule));
  }

  @Transactional(readOnly = true)
  public List<AvailabilityRuleResponse> getRules(Long resourceProductId) {
    return availabilityRuleRepository.findByResourceProductId(resourceProductId).stream()
        .map(this::toRuleResponse)
        .toList();
  }

  @Transactional
  public void deleteRule(Long ruleId) {
    availabilityRuleRepository.deleteById(ruleId);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private Booking require(Long id) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    Booking booking =
        bookingRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + id));
    TenantGuard.requireSameTenant(booking.getTenantId(), tenantId);
    return booking;
  }

  private BookingResponse toResponse(Booking b) {
    return new BookingResponse(
        b.getId(),
        b.getResourceProductId(),
        b.getResourceVariantId(),
        b.getAssignedStaffId(),
        b.getCustomerId(),
        b.getStartAt(),
        b.getEndAt(),
        b.getType().name(),
        b.getStatus().name(),
        b.getTotalPrice(),
        b.getCustomerNotes(),
        b.getCreatedAt(),
        b.getUpdatedAt());
  }

  private AvailabilityRuleResponse toRuleResponse(AvailabilityRule r) {
    return new AvailabilityRuleResponse(
        r.getId(),
        r.getResourceProductId(),
        r.getResourceVariantId(),
        r.getStaffId(),
        r.getDayOfWeek(),
        r.getOpenTime(),
        r.getCloseTime(),
        r.getSlotDurationMinutes());
  }
}
