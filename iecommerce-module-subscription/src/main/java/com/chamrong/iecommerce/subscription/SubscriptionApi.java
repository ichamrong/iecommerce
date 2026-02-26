package com.chamrong.iecommerce.subscription;

public interface SubscriptionApi {
  /** Checks if the tenant has reached their quota for a specific feature. */
  void checkQuota(String tenantId, String quotaKey, long currentCount);
}
