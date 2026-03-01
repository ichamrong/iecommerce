package com.chamrong.iecommerce.promotion.domain.port;

import com.chamrong.iecommerce.common.dto.CursorPage;
import com.chamrong.iecommerce.promotion.domain.model.Promotion;
import com.chamrong.iecommerce.promotion.domain.model.PromotionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Repository interface for Promotions. */
public interface PromotionRepository {
  Optional<Promotion> findByTenantIdAndCode(String tenantId, String code);

  Optional<Promotion> findById(Long id);

  Promotion save(Promotion promotion);

  void deleteById(Long id);

  /** Finds active promotions for a tenant within a time window. */
  List<Promotion> findAllActive(String tenantId, Instant now);

  /** Lists promotions with cursor pagination. */
  CursorPage<Promotion> findAll(String tenantId, PromotionStatus status, Long lastId, int limit);
}
