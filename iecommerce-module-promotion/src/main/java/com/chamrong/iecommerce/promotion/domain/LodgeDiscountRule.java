package com.chamrong.iecommerce.promotion.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "promotion_lodge_discount_rule")
public class LodgeDiscountRule extends BaseTenantEntity {

  @Column(nullable = false)
  private Long resourceProductId;

  /** Foreign key pointing to the specific LodgeVersion this discount belongs to. */
  @Column(nullable = false)
  private Long lodgeVersionId;

  // ── Common Fields ──────────────────────────────────────────────────────────

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private DiscountType type;

  @Column(nullable = false)
  private Double value;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ValueType valueType;

  @Column(nullable = false)
  private boolean stackable = false;

  @Column(nullable = false)
  private Integer priority = 0;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  // ── Specific Fields ────────────────────────────────────────────────────────

  // Used for SEASONAL, EARLY_BIRD_SPECIFIC, CUSTOM
  @Column(length = 255)
  private String eventName;

  @Column private LocalDate startDate; // Or "Target Event Dates"

  @Column private LocalDate endDate;

  @Column private Integer minNights; // Seasonal, Length of Stay

  @Column private Boolean recursAnnually; // Seasonal

  // Used for Booking Windows (Early Bird Gen, Last Minute, Early Bird Specific)
  @Column private Integer minDaysAdvance;

  @Column private Integer maxDaysAdvance;

  // Used for VIP / Loyalty
  @Column(length = 255)
  private String allowedTiers; // e.g. "GOLD,PLATINUM"

  @Column private Double minSpend;

  @Column private Integer minStaysQty;

  @Column private Integer lookbackPeriodMonths;

  public enum DiscountType {
    SEASONAL,
    EARLY_BIRD_SPECIFIC,
    BOOKING_WINDOW,
    LENGTH_OF_STAY,
    CUSTOM,
    CUSTOMER_TIER,
    LODGE_LOYALTY
  }

  public enum ValueType {
    PERCENT,
    FIXED
  }
}
