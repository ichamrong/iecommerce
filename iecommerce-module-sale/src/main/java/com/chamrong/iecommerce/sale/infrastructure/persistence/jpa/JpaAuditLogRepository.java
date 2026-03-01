package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaAuditLogRepository extends JpaRepository<SaleAuditLogEntity, Long> {

  @Query(
      "SELECT s FROM SaleAuditLogEntity s WHERE s.tenantId = :tenantId ORDER BY s.timestamp DESC,"
          + " s.id DESC LIMIT 1")
  Optional<SaleAuditLogEntity> findLatestByTenantId(@Param("tenantId") String tenantId);
}
