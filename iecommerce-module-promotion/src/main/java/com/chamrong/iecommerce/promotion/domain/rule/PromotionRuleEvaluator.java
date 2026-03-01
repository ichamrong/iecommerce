package com.chamrong.iecommerce.promotion.domain.rule;

import com.chamrong.iecommerce.promotion.domain.model.Promotion;
import com.chamrong.iecommerce.promotion.domain.model.PromotionRule;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Evaluates all rules for a given promotion. Uses a registry of specifications to perform the
 * actual checks.
 */
@Component
public class PromotionRuleEvaluator {

  private static final Logger log = LoggerFactory.getLogger(PromotionRuleEvaluator.class);

  private final Map<com.chamrong.iecommerce.promotion.domain.model.RuleType, PromotionSpecification>
      registry = new ConcurrentHashMap<>();

  public void register(
      com.chamrong.iecommerce.promotion.domain.model.RuleType type, PromotionSpecification spec) {
    registry.put(type, spec);
  }

  public boolean isEligible(Promotion promotion, PromotionContext context) {
    // 1. Basic Lifecycle Checks
    if (!promotion.isEligibleAt(context.getEvaluationTime())) {
      return false;
    }

    // 2. Aggregate Rule Evaluation (All rules must match)
    return promotion.getRules().stream().allMatch(rule -> evaluateRule(rule, context));
  }

  private boolean evaluateRule(PromotionRule rule, PromotionContext context) {
    PromotionSpecification spec = registry.get(rule.getType());
    if (spec == null) {
      log.warn("No specification found for rule type: {}", rule.getType());
      return true; // Unknown rules don't block by default
    }
    return spec.isSatisfiedBy(rule, context);
  }
}
