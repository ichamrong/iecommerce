package com.chamrong.iecommerce.promotion.application.port;

import com.chamrong.iecommerce.promotion.domain.model.PromotionRedemption;
import com.chamrong.iecommerce.promotion.domain.rule.PromotionContext;

/** Port for applying promotion discounts to orders. */
public interface ApplyPromotionUseCase {

  /** Reserves a discount for an order. */
  PromotionRedemption reserve(
      String tenantId,
      String code,
      String orderId,
      String customerId,
      String redemptionKey,
      PromotionContext context);

  /** Confirms the redemption (APPLIED). */
  void apply(String tenantId, String redemptionKey);

  /** Cancels the redemption (RELEASED). */
  void release(String tenantId, String redemptionKey);
}
