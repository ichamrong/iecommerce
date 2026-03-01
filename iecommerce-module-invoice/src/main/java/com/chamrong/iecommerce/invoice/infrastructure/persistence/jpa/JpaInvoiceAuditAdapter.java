package com.chamrong.iecommerce.invoice.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.invoice.domain.InvoiceAuditEntry;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceAuditRepositoryPort;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

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
