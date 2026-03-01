package com.chamrong.iecommerce.invoice.infrastructure.persistence;

import com.chamrong.iecommerce.invoice.domain.InvoiceAuditEntry;
import com.chamrong.iecommerce.invoice.domain.port.InvoiceAuditRepositoryPort;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/** Spring Data JPA interface for {@link InvoiceAuditEntry}. */
@Repository
interface SpringDataInvoiceAuditRepository extends JpaRepository<InvoiceAuditEntry, Long> {

  /** First page. */
  @Query(
      "SELECT a FROM InvoiceAuditEntry a "
          + "WHERE a.invoiceId = :invoiceId AND a.tenantId = :tenantId "
          + "ORDER BY a.occurredAt DESC, a.id DESC")
  List<InvoiceAuditEntry> findFirstPage(
      @Param("invoiceId") Long invoiceId, @Param("tenantId") String tenantId, PageRequest page);

  /** Subsequent pages using keyset cursor. */
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

/** Adapter: implements {@link InvoiceAuditRepositoryPort} via JPA. */
@Component
@RequiredArgsConstructor
class JpaInvoiceAuditAdapter implements InvoiceAuditRepositoryPort {

  private final SpringDataInvoiceAuditRepository jpaRepo;

  @Override
  public void append(InvoiceAuditEntry entry) {
    jpaRepo.save(entry);
  }

  @Override
  public List<InvoiceAuditEntry> findByInvoiceCursor(
      Long invoiceId, String tenantId, Instant afterOccurredAt, Long afterId, int limit) {
    PageRequest page = PageRequest.of(0, limit);
    if (afterOccurredAt == null || afterId == null) {
      return jpaRepo.findFirstPage(invoiceId, tenantId, page);
    }
    return jpaRepo.findNextPage(invoiceId, tenantId, afterOccurredAt, afterId, page);
  }
}
