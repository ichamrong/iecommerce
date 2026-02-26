package com.chamrong.iecommerce.booking.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a blocked calendar interval for a resource.
 *
 * <p>Used both to materialise an active booking's time reservation and to record manual blackout
 * periods (e.g., maintenance, holiday closure).
 */
@Getter
@Setter
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

  public enum BlockedReason {
    BOOKING,
    MAINTENANCE,
    PERSONAL_USE,
    BOOKED_EXTERNALLY,
    OTHER
  }
}
