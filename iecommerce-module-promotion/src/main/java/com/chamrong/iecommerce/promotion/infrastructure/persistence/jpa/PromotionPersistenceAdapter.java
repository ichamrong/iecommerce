package com.chamrong.iecommerce.promotion.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.promotion.domain.model.Promotion;
import com.chamrong.iecommerce.promotion.domain.model.PromotionStatus;
import com.chamrong.iecommerce.promotion.domain.ports.PromotionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionPersistenceAdapter implements PromotionRepository {

  private final JpaPromotionRepository jpaPromotionRepository;

  @Override
  public Optional<Promotion> findByTenantIdAndCode(String tenantId, String code) {
    return jpaPromotionRepository.findByTenantIdAndCode(tenantId, code);
  }

  @Override
  public Optional<Promotion> findById(Long id) {
    return jpaPromotionRepository.findById(id);
  }

  @Override
  public Promotion save(Promotion promotion) {
    return jpaPromotionRepository.save(promotion);
  }

  @Override
  public void deleteById(Long id) {
    jpaPromotionRepository.deleteById(id);
  }

  @Override
  public java.util.List<Promotion> findAllActive(String tenantId, java.time.Instant now) {
    return jpaPromotionRepository.findAllActive(tenantId, now);
  }

  @Override
  public List<Promotion> findPage(
      String tenantId,
      PromotionStatus status,
      Instant createdAtCursor,
      Long idCursor,
      int limitPlusOne) {
    return jpaPromotionRepository.findPage(
        tenantId, status, createdAtCursor, idCursor, PageRequest.of(0, limitPlusOne));
  }
}
