package com.chamrong.iecommerce.promotion.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.promotion.domain.model.Promotion;
import com.chamrong.iecommerce.promotion.domain.model.PromotionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPromotionRepository extends JpaRepository<Promotion, Long> {
  Optional<Promotion> findByTenantIdAndCode(String tenantId, String code);

  @Query(
      "SELECT p FROM Promotion p WHERE p.tenantId = :tenantId "
          + "AND p.status = 'ACTIVE' "
          + "AND (p.validFrom IS NULL OR p.validFrom <= :now) "
          + "AND (p.validTo IS NULL OR p.validTo >= :now)")
  List<Promotion> findAllActive(String tenantId, Instant now);

  @Query(
      "SELECT p FROM Promotion p WHERE p.tenantId = :tenantId "
          + "AND (:status IS NULL OR p.status = :status) "
          + "AND (:createdAtCursor IS NULL OR "
          + "     (p.createdAt < :createdAtCursor "
          + "      OR (p.createdAt = :createdAtCursor AND p.id < :idCursor))) "
          + "ORDER BY p.createdAt DESC, p.id DESC")
  List<Promotion> findPage(
      String tenantId,
      PromotionStatus status,
      Instant createdAtCursor,
      Long idCursor,
      Pageable pageable);
}
