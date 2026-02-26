package com.chamrong.iecommerce.subscription.domain;

import java.util.Optional;

public interface TenantSubscriptionRepository {
  TenantSubscription save(TenantSubscription subscription);

  Optional<TenantSubscription> findByTenantId(String tenantId);
}
