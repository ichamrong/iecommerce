package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.QuotationEntity;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataQuotationRepository extends JpaRepository<QuotationEntity, Long> {

  Optional<QuotationEntity> findByIdAndTenantId(Long id, String tenantId);

  @Query(
      "SELECT q FROM QuotationEntity q WHERE q.tenantId = :tenantId AND (:cursorId IS NULL OR"
          + " (q.createdAt < :cursorTime OR (q.createdAt = :cursorTime AND q.id < :cursorId)))"
          + " ORDER BY q.createdAt DESC, q.id DESC")
  Slice<QuotationEntity> findPaged(
      @Param("tenantId") String tenantId,
      @Param("cursorId") Long cursorId,
      @Param("cursorTime") Instant cursorTime,
      Pageable pageable);

  @Modifying
  @Query(
      "UPDATE QuotationEntity q SET q.status = 'CONFIRMED' WHERE q.id = :id AND q.tenantId ="
          + " :tenantId AND (q.status = 'DRAFT' OR q.status = 'SENT')")
  int confirmQuotation(@Param("tenantId") String tenantId, @Param("id") Long id);
}
