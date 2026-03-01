package com.chamrong.iecommerce.promotion.domain.model;

/** Type of discount offered by a promotion. */
public enum PromotionType {
  /** Percentage off the total (e.g., 10%). */
  PERCENTAGE,

  /** Fixed amount off (e.g., $5). */
  FIXED_AMOUNT,

  /** Free item or shipping. */
  FREE_ITEM
}
