package com.chamrong.iecommerce.review.domain;

/**
 * Canonical lifecycle status of a customer review across the moderation workflow.
 *
 * <p>This enum is used by both the persistence entity and the pure domain model to ensure a single,
 * consistent set of states.
 */
public enum ReviewStatus {
  /** Submitted but not yet reviewed by a moderator. */
  PENDING,

  /** Visible publicly on the storefront or other channels. */
  APPROVED,

  /** Explicitly rejected by a moderator and not shown to customers. */
  REJECTED,

  /**
   * Temporarily hidden from public view, for example while under investigation or during dispute
   * resolution.
   */
  HIDDEN,

  /**
   * Soft-deleted; retained for audit, analytics, or compliance, but never shown in any user-facing
   * views.
   */
  DELETED
}
