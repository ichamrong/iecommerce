package com.chamrong.iecommerce.promotion.domain.rule;

import com.chamrong.iecommerce.promotion.domain.model.PromotionRule;

/** Specification for promotion eligibility rules. */
public interface PromotionSpecification {
  /**
   * Checks if the condition is met for the given context.
   *
   * @param rule The rule configuration.
   * @param context The evaluation context (cart, customer, time).
   * @return true if eligible.
   */
  boolean isSatisfiedBy(PromotionRule rule, PromotionContext context);
}
