package com.chamrong.iecommerce.promotion.domain;

/** The mechanism by which a promotion applies its discount. */
public enum PromotionType {
  /** Reduce price by a percentage of the item or cart total (0–100%). */
  PERCENTAGE,

  /** Reduce price by a fixed monetary amount. */
  FIXED_AMOUNT,

  /** Waive the shipping fee; the discount value is ignored. */
  FREE_SHIPPING,
}
