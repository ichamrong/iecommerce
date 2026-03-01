package com.chamrong.iecommerce.promotion.application.port;

import com.chamrong.iecommerce.promotion.domain.rule.PromotionContext;
import com.chamrong.iecommerce.promotion.domain.rule.engine.PromotionEngine;

/** Port for validating promotion eligibility and calculating potential discounts. */
public interface ValidatePromotionUseCase {
  /**
   * Finds and validates a promotion by code.
   *
   * @param tenantId The tenant ID.
   * @param code The voucher code.
   * @param context The evaluation context.
   * @return true if valid and eligible.
   */
  boolean validate(String tenantId, String code, PromotionContext context);

  /** Calculates pricing results for a context. */
  PromotionEngine.PricingResult calculate(PromotionContext context);
}
