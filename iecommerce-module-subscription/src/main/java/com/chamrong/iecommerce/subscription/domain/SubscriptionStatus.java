package com.chamrong.iecommerce.subscription.domain;

public enum SubscriptionStatus {
  TRIAL,
  ACTIVE,
  /** Billing grace period — read-only API access. */
  GRACE,
  EXPIRED,
  CANCELLED,
  SUSPENDED
}
