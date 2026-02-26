package com.chamrong.iecommerce.subscription.domain;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository {
  SubscriptionPlan save(SubscriptionPlan plan);

  Optional<SubscriptionPlan> findById(Long id);

  Optional<SubscriptionPlan> findByCode(String code);

  List<SubscriptionPlan> findAll();

  List<SubscriptionPlan> findByActiveTrue();
}
