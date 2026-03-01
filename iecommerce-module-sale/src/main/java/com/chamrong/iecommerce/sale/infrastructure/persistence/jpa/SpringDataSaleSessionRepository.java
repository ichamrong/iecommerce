package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.SaleSessionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataSaleSessionRepository extends JpaRepository<SaleSessionEntity, Long> {

  Optional<SaleSessionEntity> findByIdAndTenantId(Long id, String tenantId);

  @Query(
      "SELECT s FROM SaleSessionEntity s WHERE s.tenantId = :tenantId AND s.terminalId ="
          + " :terminalId AND s.status = 'OPEN'")
  Optional<SaleSessionEntity> findActiveSession(
      @Param("tenantId") String tenantId, @Param("terminalId") String terminalId);

  @Query(
      "SELECT s FROM SaleSessionEntity s WHERE s.tenantId = :tenantId AND (:terminalId IS NULL OR"
          + " s.terminalId = :terminalId) AND (:cursorId IS NULL OR (s.createdAt < :cursorTime OR"
          + " (s.createdAt = :cursorTime AND s.id < :cursorId))) ORDER BY s.createdAt DESC, s.id"
          + " DESC")
  List<SaleSessionEntity> findPaged(
      @Param("tenantId") String tenantId,
      @Param("terminalId") String terminalId,
      @Param("cursorId") Long cursorId,
      @Param("cursorTime") java.time.Instant cursorTime,
      Pageable pageable);
}
