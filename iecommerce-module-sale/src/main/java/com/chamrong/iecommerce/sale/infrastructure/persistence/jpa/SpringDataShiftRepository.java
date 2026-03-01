package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.ShiftEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataShiftRepository extends JpaRepository<ShiftEntity, Long> {

  Optional<ShiftEntity> findByIdAndTenantId(Long id, String tenantId);

  @Query(
      "SELECT s FROM ShiftEntity s WHERE s.tenantId = :tenantId AND s.staffId = :staffId "
          + "AND s.terminalId = :terminalId AND s.status = 'OPEN'")
  Optional<ShiftEntity> findActiveShift(
      @Param("tenantId") String tenantId,
      @Param("staffId") String staffId,
      @Param("terminalId") String terminalId);

  @Query(
      "SELECT s FROM ShiftEntity s WHERE s.tenantId = :tenantId AND (:cursorId IS NULL OR"
          + " (s.createdAt < :cursorTime OR (s.createdAt = :cursorTime AND s.id < :cursorId)))"
          + " ORDER BY s.createdAt DESC, s.id DESC")
  List<ShiftEntity> findPaged(
      @Param("tenantId") String tenantId,
      @Param("cursorId") Long cursorId,
      @Param("cursorTime") java.time.Instant cursorTime,
      Pageable pageable);
}
