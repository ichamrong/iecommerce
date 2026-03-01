package com.chamrong.iecommerce.promotion.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.common.dto.CursorPage;
import com.chamrong.iecommerce.promotion.domain.model.Promotion;
import com.chamrong.iecommerce.promotion.domain.model.PromotionStatus;
import com.chamrong.iecommerce.promotion.domain.ports.PromotionRepository;
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
  public CursorPage<Promotion> findAll(
      String tenantId, PromotionStatus status, Long lastId, int limit) {
    List<Promotion> results =
        jpaPromotionRepository.findWithCursor(
            tenantId, status, lastId, PageRequest.of(0, limit + 1));

    boolean hasMore = results.size() > limit;
    List<Promotion> pageData = hasMore ? results.subList(0, limit) : results;
    String nextCursor =
        pageData.isEmpty() ? null : String.valueOf(pageData.get(pageData.size() - 1).getId());

    return CursorPage.of(pageData, nextCursor, hasMore);
  }
}
