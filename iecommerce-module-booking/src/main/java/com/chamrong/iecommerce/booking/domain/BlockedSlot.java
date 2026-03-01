package com.chamrong.iecommerce.booking.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a blocked calendar interval for a resource.
 *
 * <p>Used both to materialise an active booking's time reservation and to record manual blackout
 * periods (e.g., maintenance, holiday closure).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "booking_blocked_slot")
public class BlockedSlot extends BaseEntity {

  @Column(nullable = false)
  private Long resourceProductId;

  @Column private Long resourceVariantId;

  @Column(nullable = false)
  private Instant startAt;

  @Column(nullable = false)
  private Instant endAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BlockedReason reason;

  @Column(columnDefinition = "TEXT")
  private String note;

  @Column(length = 50)
  private String sourcePlatform;

  public static BlockedSlot forBooking(
      Long resourceProductId, Long resourceVariantId, Instant startAt, Instant endAt) {
    var slot = new BlockedSlot();
    slot.resourceProductId = resourceProductId;
    slot.resourceVariantId = resourceVariantId;
    slot.startAt = startAt;
    slot.endAt = endAt;
    slot.reason = BlockedReason.BOOKING;
    return slot;
  }

  public static BlockedSlot forMaintenance(
      Long resourceProductId, Instant startAt, Instant endAt, String note) {
    var slot = new BlockedSlot();
    slot.resourceProductId = resourceProductId;
    slot.startAt = startAt;
    slot.endAt = endAt;
    slot.reason = BlockedReason.MAINTENANCE;
    slot.note = note;
    return slot;
  }

  public enum BlockedReason {
    BOOKING,
    MAINTENANCE,
    PERSONAL_USE,
    BOOKED_EXTERNALLY,
    OTHER
  }
}
