package com.chamrong.iecommerce.promotion.domain.rule.evaluator;

import com.chamrong.iecommerce.promotion.domain.rule.PromotionContext;
import com.chamrong.iecommerce.promotion.domain.rule.dsl.RuleDefinition;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
class CartSubtotalConditionEvaluator
    implements ConditionEvaluator<RuleDefinition.CartSubtotalCondition> {
  @Override
  public boolean evaluate(
      RuleDefinition.CartSubtotalCondition condition, PromotionContext context) {
    if (context.getBaseAmount() == null) return false;
    BigDecimal subtotal = context.getBaseAmount().getAmount();
    BigDecimal threshold = condition.getMinAmount();

    return switch (condition.getOperator()) {
      case "GTE" -> subtotal.compareTo(threshold) >= 0;
      case "GT" -> subtotal.compareTo(threshold) > 0;
      default -> false;
    };
  }

  @Override
  public Class<RuleDefinition.CartSubtotalCondition> getConditionType() {
    return RuleDefinition.CartSubtotalCondition.class;
  }
}

@Component
class PercentageDiscountActionEvaluator
    implements ActionEvaluator<RuleDefinition.PercentageDiscountAction> {
  @Override
  public BigDecimal compute(
      RuleDefinition.PercentageDiscountAction action, PromotionContext context) {
    if (context.getBaseAmount() == null) return BigDecimal.ZERO;

    BigDecimal base = context.getBaseAmount().getAmount();
    BigDecimal percentage = action.getPercentage();
    BigDecimal discount =
        base.multiply(percentage).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

    if (action.getMaxCap() != null && discount.compareTo(action.getMaxCap()) > 0) {
      return action.getMaxCap();
    }
    return discount;
  }

  @Override
  public Class<RuleDefinition.PercentageDiscountAction> getActionType() {
    return RuleDefinition.PercentageDiscountAction.class;
  }
}

@Component
class AmountDiscountActionEvaluator
    implements ActionEvaluator<RuleDefinition.AmountDiscountAction> {
  @Override
  public BigDecimal compute(RuleDefinition.AmountDiscountAction action, PromotionContext context) {
    if (context.getBaseAmount() == null) return BigDecimal.ZERO;
    return context.getBaseAmount().getAmount().min(action.getAmount());
  }

  @Override
  public Class<RuleDefinition.AmountDiscountAction> getActionType() {
    return RuleDefinition.AmountDiscountAction.class;
  }
}
