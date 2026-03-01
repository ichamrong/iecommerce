package com.chamrong.iecommerce.promotion.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.promotion.domain.model.PromotionRedemption;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPromotionRedemptionRepository extends JpaRepository<PromotionRedemption, Long> {
  Optional<PromotionRedemption> findByTenantIdAndRedemptionKey(
      String tenantId, String redemptionKey);
}
