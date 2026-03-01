package com.chamrong.iecommerce.promotion.domain.rule.evaluator;

import com.chamrong.iecommerce.promotion.domain.rule.PromotionContext;
import com.chamrong.iecommerce.promotion.domain.rule.dsl.RuleDefinition;
import java.math.BigDecimal;

/** Interface for computing potential discounts for specific actions in the DSL. */
public interface ActionEvaluator<T extends RuleDefinition.Action> {
  /** returns the potential discount amount. */
  BigDecimal compute(T action, PromotionContext context);

  Class<T> getActionType();
}
