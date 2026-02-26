package com.chamrong.iecommerce.review.domain;

/** Lifecycle status of a customer product review. */
public enum ReviewStatus {
  /** Submitted but not yet reviewed by a moderator. */
  PENDING,
  /** Visible publicly on the storefront. */
  APPROVED,
  /** Hidden — failed moderation. */
  REJECTED
}
