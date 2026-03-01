package com.chamrong.iecommerce.promotion.domain.rule.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Factory for selecting the appropriate SelectionPolicy. */
@Component
@RequiredArgsConstructor
public class SelectionPolicyFactory {

  private final BestSavingsPolicy bestSavingsPolicy;
  private final PriorityFirstPolicy priorityFirstPolicy;

  public SelectionPolicy getPolicy(String tenantId) {
    // In a real system, this could look up tenant settings in a DB or Cache
    // For now, we'll default to BestSavings or use a simple logic
    return bestSavingsPolicy;
  }
}
