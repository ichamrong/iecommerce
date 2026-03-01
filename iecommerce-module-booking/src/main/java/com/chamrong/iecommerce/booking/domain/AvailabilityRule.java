package com.chamrong.iecommerce.booking.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Defines the recurring availability schedule for a resource.
 *
 * <p>Each row represents one day-of-week window (e.g., Monday 09:00–18:00). Multiple rows per
 * resource + day are supported for split schedules (e.g., 09:00–12:00 and 14:00–18:00).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "booking_availability_rule")
public class AvailabilityRule extends BaseTenantEntity {

  @Column(nullable = false)
  private Long resourceProductId;

  @Column private Long resourceVariantId;

  @Column private Long staffId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private DayOfWeek dayOfWeek;

  @Column(nullable = false)
  private LocalTime openTime;

  @Column(nullable = false)
  private LocalTime closeTime;

  /** Slot duration in minutes (relevant for APPOINTMENT types). */
  @Column(nullable = false)
  private Integer slotDurationMinutes = 60;

  /** Minimum nights for ACCOMMODATION type. */
  private Integer minStayNights;

  /** Maximum nights for ACCOMMODATION type. */
  private Integer maxStayNights;

  public static AvailabilityRule of(
      String tenantId,
      Long resourceProductId,
      Long resourceVariantId,
      Long staffId,
      DayOfWeek dayOfWeek,
      LocalTime openTime,
      LocalTime closeTime,
      Integer slotDurationMinutes) {
    var rule = new AvailabilityRule();
    rule.setTenantId(tenantId);
    rule.resourceProductId = resourceProductId;
    rule.resourceVariantId = resourceVariantId;
    rule.staffId = staffId;
    rule.dayOfWeek = dayOfWeek;
    rule.openTime = openTime;
    rule.closeTime = closeTime;
    rule.slotDurationMinutes = slotDurationMinutes != null ? slotDurationMinutes : 60;
    return rule;
  }
}
