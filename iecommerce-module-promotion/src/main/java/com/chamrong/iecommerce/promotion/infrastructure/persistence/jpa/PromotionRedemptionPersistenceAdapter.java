package com.chamrong.iecommerce.promotion.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.promotion.domain.model.PromotionRedemption;
import com.chamrong.iecommerce.promotion.domain.ports.PromotionRedemptionRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionRedemptionPersistenceAdapter implements PromotionRedemptionRepository {

  private final JpaPromotionRedemptionRepository jpaRedemptionRepository;

  @Override
  public Optional<PromotionRedemption> findByRedemptionKey(String tenantId, String redemptionKey) {
    return jpaRedemptionRepository.findByTenantIdAndRedemptionKey(tenantId, redemptionKey);
  }

  @Override
  public Optional<PromotionRedemption> findById(Long id) {
    return jpaRedemptionRepository.findById(id);
  }

  @Override
  public PromotionRedemption save(PromotionRedemption redemption) {
    return jpaRedemptionRepository.save(redemption);
  }
}
