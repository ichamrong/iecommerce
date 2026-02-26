package com.chamrong.iecommerce.promotion.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "promotion")
public class Promotion extends BaseTenantEntity {

  @Column(nullable = false, length = 255)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private PromotionType type;

  /** Discount value — percentage (0–100) for PERCENTAGE type, fixed monetary amount for FIXED. */
  @Column(nullable = false)
  private Double value;

  private Instant validFrom;
  private Instant validTo;

  /** Optional voucher code customers must enter to activate this promotion. */
  @Column(name = "code", length = 50, unique = true)
  private String code;

  @Column(nullable = false)
  private boolean active = true;

  // ── Domain behaviour ───────────────────────────────────────────────────────

  /** Returns true if the promotion is currently valid at the given instant. */
  public boolean isActiveAt(Instant at) {
    if (!active) return false;
    if (validFrom != null && at.isBefore(validFrom)) return false;
    if (validTo != null && at.isAfter(validTo)) return false;
    return true;
  }

  /**
   * Calculates the discount amount for a given base price.
   *
   * @param baseAmount the price to apply the discount to
   * @return discount amount (not negative; capped at baseAmount for PERCENTAGE)
   */
  public java.math.BigDecimal calculateDiscount(java.math.BigDecimal baseAmount) {
    return switch (type) {
      case PERCENTAGE ->
          baseAmount
              .multiply(java.math.BigDecimal.valueOf(value / 100.0))
              .min(baseAmount)
              .setScale(4, java.math.RoundingMode.HALF_UP);
      case FIXED_AMOUNT -> java.math.BigDecimal.valueOf(value).min(baseAmount);
      case FREE_SHIPPING -> java.math.BigDecimal.ZERO; // handled at checkout
    };
  }
}
