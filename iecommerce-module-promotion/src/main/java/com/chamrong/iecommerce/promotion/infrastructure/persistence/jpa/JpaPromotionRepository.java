package com.chamrong.iecommerce.promotion.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.promotion.domain.model.Promotion;
import com.chamrong.iecommerce.promotion.domain.model.PromotionStatus;
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
  List<Promotion> findAllActive(String tenantId, java.time.Instant now);

  @Query(
      "SELECT p FROM Promotion p WHERE p.tenantId = :tenantId "
          + "AND (:status IS NULL OR p.status = :status) "
          + "AND (:lastId IS NULL OR p.id > :lastId) "
          + "ORDER BY p.id ASC")
  List<Promotion> findWithCursor(
      String tenantId, PromotionStatus status, Long lastId, Pageable pageable);
}
