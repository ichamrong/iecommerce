package com.chamrong.iecommerce.promotion.domain.model;

/** Promotion lifecycle states. */
public enum PromotionStatus {
  /** Initial state, being configured. Not visible to customers. */
  DRAFT,

  /** Active and eligible for application (if within date ranges). */
  ACTIVE,

  /** Manually suspended by admin. */
  PAUSED,

  /** Reached its end date or usage limit. */
  EXPIRED,

  /** No longer in use, kept for historical audit. */
  ARCHIVED
}
