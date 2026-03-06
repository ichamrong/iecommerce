package com.chamrong.iecommerce.booking.api;

import com.chamrong.iecommerce.booking.application.BookingService;
import com.chamrong.iecommerce.booking.application.dto.AvailabilityRuleRequest;
import com.chamrong.iecommerce.booking.application.dto.AvailabilityRuleResponse;
import com.chamrong.iecommerce.booking.application.dto.AvailableSlot;
import com.chamrong.iecommerce.booking.application.dto.BookingResponse;
import com.chamrong.iecommerce.booking.application.dto.CreateBookingRequest;
import com.chamrong.iecommerce.common.security.CapabilityGate;
import com.chamrong.iecommerce.common.security.TenantGuard;
import com.chamrong.iecommerce.setting.domain.ModuleCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Booking reservation and availability management.
 *
 * <p>Supports both appointment-style (service hour slots) and accommodation-style (nightly stay
 * date ranges) bookings.
 *
 * <p>Base path: {@code /api/v1/bookings}
 */
@Tag(name = "Bookings", description = "Reservation management and availability calendar")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BookingController {

  private final BookingService bookingService;
  private final CapabilityGate capabilityGate;

  // ── Booking CRUD & lifecycle ───────────────────────────────────────────────

  @Operation(
      summary = "Create a booking",
      description =
          "Reserves a resource slot. Automatically blocks the time window and checks for"
              + " conflicts.")
  @PostMapping
  public ResponseEntity<BookingResponse> create(@RequestBody CreateBookingRequest req) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(bookingService.createBooking(tenantId, req));
  }

  @Operation(summary = "Get booking by ID")
  @GetMapping("/{id}")
  public ResponseEntity<BookingResponse> getById(@PathVariable Long id) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    return bookingService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get all bookings for a customer")
  @GetMapping("/customers/{customerId}")
  public List<BookingResponse> getByCustomer(@PathVariable Long customerId) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    return bookingService.getCustomerBookings(tenantId, customerId);
  }

  @Operation(summary = "Get accepted bookings for a resource")
  @GetMapping("/resources/{resourceProductId}/accepted")
  @PreAuthorize("hasAuthority('bookings:manage')")
  public List<BookingResponse> getAcceptedByResource(@PathVariable Long resourceProductId) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    return bookingService.getResourceAcceptedBookings(tenantId, resourceProductId);
  }

  // ── State transitions ──────────────────────────────────────────────────────

  @Operation(
      summary = "Accept a booking",
      description = "Provider accepts PENDING → ACCEPTED. Fires BookingConfirmedEvent.")
  @PostMapping("/{id}/accept")
  @PreAuthorize("hasAuthority('bookings:manage')")
  public BookingResponse accept(@PathVariable Long id) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    return bookingService.accept(id);
  }

  @Operation(
      summary = "Reject a booking",
      description = "Provider rejects PENDING → REJECTED. Triggers refund.")
  @PostMapping("/{id}/reject")
  @PreAuthorize("hasAuthority('bookings:manage')")
  public BookingResponse reject(@PathVariable Long id, @RequestParam String reason) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    return bookingService.reject(id, reason);
  }

  @Operation(
      summary = "Check-in guest",
      description = "Marks an ACCEPTED booking as CHECKIN. Triggers payout.")
  @PostMapping("/{id}/checkin")
  @PreAuthorize("hasAuthority('bookings:manage')")
  public BookingResponse checkIn(@PathVariable Long id) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    return bookingService.checkIn(id);
  }

  @Operation(
      summary = "Check-out guest",
      description = "Marks a CHECKIN booking as CHECKOUT (completed).")
  @PostMapping("/{id}/checkout")
  @PreAuthorize("hasAuthority('bookings:manage')")
  public BookingResponse checkOut(@PathVariable Long id) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    return bookingService.checkOut(id);
  }

  @Operation(
      summary = "Cancel booking",
      description = "Guest or Provider cancels a booking. Triggers refund calculation.")
  @PostMapping("/{id}/cancel")
  public BookingResponse cancel(
      @PathVariable Long id, @RequestParam(required = false) String reason) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    return bookingService.cancel(id, reason);
  }

  // ── Availability calendar ──────────────────────────────────────────────────

  @Operation(
      summary = "Check available slots",
      description =
          "Returns all time slots for a resource on the given date, flagged as available or taken.")
  @GetMapping("/availability/{resourceProductId}")
  public List<AvailableSlot> getAvailability(
      @PathVariable Long resourceProductId,
      @RequestParam(required = false) Long staffId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    return bookingService.getAvailableSlots(tenantId, resourceProductId, staffId, date);
  }

  // ── Availability rules (admin) ─────────────────────────────────────────────

  @Operation(
      summary = "Add availability rule",
      description = "Defines a recurring weekly schedule window for a resource.")
  @PostMapping("/availability-rules")
  @PreAuthorize("hasAuthority('bookings:manage')")
  public ResponseEntity<AvailabilityRuleResponse> addRule(
      @RequestBody AvailabilityRuleRequest req) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.addRule(tenantId, req));
  }

  @Operation(summary = "List availability rules for a resource")
  @GetMapping("/availability-rules/{resourceProductId}")
  @PreAuthorize("hasAuthority('bookings:manage')")
  public List<AvailabilityRuleResponse> getRules(@PathVariable Long resourceProductId) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    return bookingService.getRules(tenantId, resourceProductId);
  }

  @Operation(summary = "Delete an availability rule")
  @DeleteMapping("/availability-rules/{ruleId}")
  @PreAuthorize("hasAuthority('bookings:manage')")
  public ResponseEntity<Void> deleteRule(@PathVariable Long ruleId) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    capabilityGate.requireModule(tenantId, ModuleCodes.BOOKING);
    bookingService.deleteRule(tenantId, ruleId);
    return ResponseEntity.noContent().build();
  }
}
