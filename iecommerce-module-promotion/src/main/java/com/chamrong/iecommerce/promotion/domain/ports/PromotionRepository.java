package com.chamrong.iecommerce.promotion.domain.ports;

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

  /**
   * Lists promotions for cursor-based pagination using keyset (createdAt, id).
   *
   * @param tenantId tenant scope
   * @param status optional status filter
   * @param createdAtCursor createdAt boundary (exclusive) or null for first page
   * @param idCursor id boundary (exclusive) or null for first page
   * @param limitPlusOne query limit (requested page size + 1 to detect hasNext)
   * @return up to {@code limitPlusOne} promotions sorted by createdAt DESC, id DESC
   */
  List<Promotion> findPage(
      String tenantId,
      PromotionStatus status,
      Instant createdAtCursor,
      Long idCursor,
      int limitPlusOne);
}
