package com.chamrong.iecommerce.booking.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * A Booking represents one confirmed reservation of a resource (room or service slot) by a customer
 * for a specific time window.
 *
 * <p>For APPOINTMENT bookings: {@code startAt} and {@code endAt} are timestamps (minute-precise).
 * For ACCOMMODATION bookings: {@code startAt} = check-in date midnight, {@code endAt} = check-out
 * date midnight.
 */
@Getter
@Setter
@Entity
@Table(name = "booking")
public class Booking extends BaseTenantEntity {

  // ── What is being booked ───────────────────────────────────────────────────

  /** Product (service or accommodation) from the Catalog module. */
  @Column(nullable = false)
  private Long resourceProductId;

  /**
   * Optional — the specific variant (e.g., room type variant, treatment variant) being reserved.
   */
  @Column private Long resourceVariantId;

  /** Immutable snapshot version the customer viewed at the time of booking. */
  @Column private Long lodgeVersionId;

  /** CSV list of applied LodgeDiscountRule IDs. */
  @Column(length = 255)
  private String appliedDiscountRuleIds;

  /** Optionally pin to a specific staff member (e.g., preferred therapist). */
  @Column private Long assignedStaffId;

  // ── Who booked ─────────────────────────────────────────────────────────────

  @Column(nullable = false)
  private Long customerId;

  // ── Specific Guest Details (for Check-in Verification) ─────────────────────

  @Column(length = 255)
  private String guestFirstName;

  @Column(length = 255)
  private String guestLastName;

  @Column(length = 255)
  private String guestEmail;

  @Column(length = 50)
  private String guestPhone;

  @Column(length = 50)
  private String guestTelegram;

  // ── When ───────────────────────────────────────────────────────────────────

  @Column(nullable = false)
  private Instant startAt;

  @Column(nullable = false)
  private Instant endAt;

  // ── Type & status ──────────────────────────────────────────────────────────

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BookingType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private BookingStatus status = BookingStatus.PENDING;

  // ── Pricing ────────────────────────────────────────────────────────────────

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "total_price_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "total_price_currency"))
  })
  private Money totalPrice;

  // ── Notes ──────────────────────────────────────────────────────────────────

  @Column(columnDefinition = "TEXT")
  private String customerNotes;

  @Column(columnDefinition = "TEXT")
  private String internalNotes;

  // ── Slot blocking records ──────────────────────────────────────────────────

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "booking_id")
  private List<BlockedSlot> blockedSlots = new ArrayList<>();

  // ── Domain behaviour ───────────────────────────────────────────────────────

  public void accept() {
    requireStatus(BookingStatus.PENDING, "accept");
    this.status = BookingStatus.ACCEPTED;
  }

  public void reject(String reason) {
    requireStatus(BookingStatus.PENDING, "reject");
    this.status = BookingStatus.REJECTED;
    this.internalNotes = reason;
  }

  public void checkIn() {
    requireStatus(BookingStatus.ACCEPTED, "check-in");
    this.status = BookingStatus.CHECKIN;
  }

  public void checkOut() {
    requireStatus(BookingStatus.CHECKIN, "check-out");
    this.status = BookingStatus.CHECKOUT;
  }

  public void cancel(String reason) {
    if (status == BookingStatus.CHECKOUT || status == BookingStatus.CANCEL) {
      throw new IllegalStateException("Cannot cancel a booking in state: " + status);
    }
    this.status = BookingStatus.CANCEL;
    if (reason != null && !reason.isBlank()) {
      this.internalNotes = reason;
    }
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private void requireStatus(BookingStatus required, String operation) {
    if (this.status != required) {
      throw new IllegalStateException(
          "Cannot "
              + operation
              + " a booking in state: "
              + this.status
              + " (required: "
              + required
              + ")");
    }
  }
}
