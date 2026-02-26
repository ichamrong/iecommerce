package com.chamrong.iecommerce.subscription.infrastructure;

import com.chamrong.iecommerce.subscription.domain.SubscriptionPlan;
import com.chamrong.iecommerce.subscription.domain.SubscriptionPlanRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSubscriptionPlanRepository
    extends JpaRepository<SubscriptionPlan, Long>, SubscriptionPlanRepository {
  @Override
  Optional<SubscriptionPlan> findByCode(String code);
}
