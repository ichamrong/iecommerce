package com.chamrong.iecommerce.promotion.domain.rule.evaluator;

import com.chamrong.iecommerce.promotion.domain.rule.PromotionContext;
import com.chamrong.iecommerce.promotion.domain.rule.dsl.RuleDefinition;

/** Interface for evaluating specific conditions in the DSL. */
public interface ConditionEvaluator<T extends RuleDefinition.Condition> {
  boolean evaluate(T condition, PromotionContext context);

  Class<T> getConditionType();
}
