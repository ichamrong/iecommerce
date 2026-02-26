package com.chamrong.iecommerce.promotion;

import com.chamrong.iecommerce.common.Money;
import java.util.Optional;

/** Public API of the Promotion module. */
public interface PromotionApi {

  /**
   * Applies a promotion code to a base amount. Returns the discount amount if the code is valid.
   */
  Optional<Money> calculateDiscount(String tenantId, String code, Money baseAmount);
}
