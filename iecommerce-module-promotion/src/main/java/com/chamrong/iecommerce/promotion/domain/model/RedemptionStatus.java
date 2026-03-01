package com.chamrong.iecommerce.promotion.domain.model;

/** Status of a specific promotion redemption transaction. */
public enum RedemptionStatus {
  /** Discount calculated and held for an order in progress. */
  RESERVED,

  /** Order completed, discount permanently applied. */
  APPLIED,

  /** Order cancelled or expired, discount released back to the pool. */
  RELEASED
}
