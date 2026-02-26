package com.chamrong.iecommerce.promotion;

import com.chamrong.iecommerce.common.Money;
import java.util.Optional;

/** Public API of the Promotion module. */
public interface PromotionApi {

  /**
   * Applies a promotion code to a base amount. Returns the discount amount if the code is valid.
   */
  Optional<Money> calculateDiscount(String tenantId, String code, Money baseAmount);

  /**
   * Advanced discount calculation considering business-specific context (items, booking dates,
   * etc).
   */
  default Optional<Money> calculateDiscount(
      String code, com.chamrong.iecommerce.promotion.application.rule.PromotionContext context) {
    return calculateDiscount(context.getTenantId(), code, context.getBaseAmount());
  }
}
