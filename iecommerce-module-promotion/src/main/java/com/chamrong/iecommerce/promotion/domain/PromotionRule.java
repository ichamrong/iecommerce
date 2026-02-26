package com.chamrong.iecommerce.promotion.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "promotion_rules")
public class PromotionRule extends BaseTenantEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "promotion_id")
  private Promotion promotion;

  @Enumerated(EnumType.STRING)
  private RuleType type;

  /**
   * Data for the rule (e.g., product IDs, category names, min quantity, time range). Could be JSON
   * or a simple string.
   */
  @Column(columnDefinition = "TEXT")
  private String ruleData;

  public enum RuleType {
    // Retail
    PRODUCT_IN_LIST,
    CATEGORY_MATCH,
    MIN_PURCHASE_QUANTITY,

    // Hospitality
    MIN_NIGHTS_STAY,
    EARLY_BIRD_DAYS,
    STAY_WITHIN_DATES,

    // F&B / General
    TIME_OF_DAY_RANGE,
    DAY_OF_WEEK,

    // Geographic / Logistic
    REGION_MATCH, // e.g., "Phnom Penh, Siem Reap"
    DELIVERY_ZONE, // e.g., "Zone-A, Zone-B"
    STORE_PICKUP_ONLY,

    // Customer
    CUSTOMER_SEGMENT,
    FIRST_TIME_BUYER
  }
}
