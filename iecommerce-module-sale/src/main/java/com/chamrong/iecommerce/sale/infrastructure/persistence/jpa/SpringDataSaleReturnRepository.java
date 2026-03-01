package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.SaleReturnEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataSaleReturnRepository extends JpaRepository<SaleReturnEntity, Long> {

  Optional<SaleReturnEntity> findByIdAndTenantId(Long id, String tenantId);

  Optional<SaleReturnEntity> findByTenantIdAndReturnKey(String tenantId, String returnKey);

  @Query(
      "SELECT r FROM SaleReturnEntity r WHERE r.tenantId = :tenantId AND (:cursorId IS NULL OR"
          + " (r.createdAt < :cursorTime OR (r.createdAt = :cursorTime AND r.id < :cursorId)))"
          + " ORDER BY r.createdAt DESC, r.id DESC")
  List<SaleReturnEntity> findPaged(
      @Param("tenantId") String tenantId,
      @Param("cursorId") Long cursorId,
      @Param("cursorTime") java.time.Instant cursorTime,
      Pageable pageable);
}
