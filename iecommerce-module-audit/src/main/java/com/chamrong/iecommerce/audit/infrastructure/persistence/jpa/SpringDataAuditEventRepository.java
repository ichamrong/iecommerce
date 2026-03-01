package com.chamrong.iecommerce.audit.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for audit_event. Keyset queries via custom methods and
 * JpaAuditEventRepositoryAdapter.
 */
@Repository
public interface SpringDataAuditEventRepository
    extends JpaRepository<AuditEventEntity, Long>,
        JpaSpecificationExecutor<AuditEventEntity> {

  List<AuditEventEntity> findByTenantIdOrderByCreatedAtDescIdDesc(
      String tenantId, Pageable pageable);

  /** Previous event in chain: the one immediately before (createdAt, id) in insert order. */
  @Query(
      """
      SELECT e FROM AuditEventEntity e
      WHERE e.tenantId = :tenantId
        AND (e.createdAt < :createdAt OR (e.createdAt = :createdAt AND e.id < :id))
      ORDER BY e.createdAt DESC, e.id DESC
      """)
  List<AuditEventEntity> findPreviousInChain(
      @Param("tenantId") String tenantId,
      @Param("createdAt") Instant createdAt,
      @Param("id") Long id,
      Pageable pageable);
}
