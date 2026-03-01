package com.chamrong.iecommerce.promotion.domain.ports;

import com.chamrong.iecommerce.promotion.domain.model.PromotionRedemption;
import java.util.Optional;

/** Repository interface for Redemptions. */
public interface PromotionRedemptionRepository {
  Optional<PromotionRedemption> findByRedemptionKey(String tenantId, String redemptionKey);

  Optional<PromotionRedemption> findById(Long id);

  PromotionRedemption save(PromotionRedemption redemption);
}
