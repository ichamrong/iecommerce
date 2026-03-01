package com.chamrong.iecommerce.invoice.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.invoice.domain.InvoiceAuditEntry;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA interface for {@link InvoiceAuditEntry}. */
@Repository
interface SpringDataInvoiceAuditRepository extends JpaRepository<InvoiceAuditEntry, Long> {

  @Query(
      "SELECT a FROM InvoiceAuditEntry a "
          + "WHERE a.invoiceId = :invoiceId AND a.tenantId = :tenantId "
          + "ORDER BY a.occurredAt DESC, a.id DESC")
  List<InvoiceAuditEntry> findFirstPage(
      @Param("invoiceId") Long invoiceId, @Param("tenantId") String tenantId, PageRequest page);

  @Query(
      "SELECT a FROM InvoiceAuditEntry a "
          + "WHERE a.invoiceId = :invoiceId AND a.tenantId = :tenantId "
          + "AND (a.occurredAt < :after OR (a.occurredAt = :after AND a.id < :afterId)) "
          + "ORDER BY a.occurredAt DESC, a.id DESC")
  List<InvoiceAuditEntry> findNextPage(
      @Param("invoiceId") Long invoiceId,
      @Param("tenantId") String tenantId,
      @Param("after") Instant after,
      @Param("afterId") Long afterId,
      PageRequest page);
}
