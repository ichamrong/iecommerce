package com.chamrong.iecommerce.subscription.infrastructure;

import com.chamrong.iecommerce.subscription.domain.TenantSubscription;
import com.chamrong.iecommerce.subscription.domain.TenantSubscriptionRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTenantSubscriptionRepository
    extends JpaRepository<TenantSubscription, Long>, TenantSubscriptionRepository {
  @Override
  Optional<TenantSubscription> findByTenantId(String tenantId);
}
