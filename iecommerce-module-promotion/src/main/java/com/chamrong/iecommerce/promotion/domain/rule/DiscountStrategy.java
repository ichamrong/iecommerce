package com.chamrong.iecommerce.promotion.domain.rule;

import java.math.BigDecimal;

/** Strategy for calculating discount value. */
public interface DiscountStrategy {
  /**
   * Calculates discount amount based on base total.
   *
   * @param baseAmount The total amount before discount.
   * @param promotionValue The configured promotion value (e.g. 10.0 for 10% or $10).
   * @return The discount amount.
   */
  BigDecimal calculate(BigDecimal baseAmount, BigDecimal promotionValue);
}
