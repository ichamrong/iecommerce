package com.chamrong.iecommerce.promotion.domain.rule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/** Concrete implementations of discount strategies. */
@Component
public class DiscountStrategies {

  @Component
  public static class PercentageDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculate(BigDecimal baseAmount, BigDecimal promotionValue) {
      return baseAmount
          .multiply(promotionValue)
          .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }
  }

  @Component
  public static class FixedAmountDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculate(BigDecimal baseAmount, BigDecimal promotionValue) {
      return baseAmount.min(promotionValue); // Cannot discount more than the base amount
    }
  }
}
