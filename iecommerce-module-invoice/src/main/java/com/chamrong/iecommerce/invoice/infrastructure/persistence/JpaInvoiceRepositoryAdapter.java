package com.chamrong.iecommerce.invoice.infrastructure.persistence;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceStatus;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing {@link InvoiceRepositoryPort}.
 *
 * <p>Delegates to {@link SpringDataInvoiceRepository} and routes cursor pagination to the correct
 * JPQL query.
 */
@Component
@RequiredArgsConstructor
public class JpaInvoiceRepositoryAdapter implements InvoiceRepositoryPort {

  private final SpringDataInvoiceRepository jpaRepo;

  @Override
  public Invoice save(Invoice invoice) {
    return jpaRepo.save(invoice);
  }

  @Override
  public Optional<Invoice> findByIdAndTenant(Long id, String tenantId) {
    return jpaRepo.findByIdAndTenantId(id, tenantId);
  }

  @Override
  public boolean existsByTenantAndInvoiceNumber(String tenantId, String invoiceNumber) {
    return jpaRepo.existsByTenantIdAndInvoiceNumber(tenantId, invoiceNumber);
  }

  @Override
  public List<Invoice> findByTenantCursor(
      String tenantId, InvoiceStatus statusFilter, Instant afterIssuedAt, Long afterId, int limit) {
    PageRequest page = PageRequest.of(0, limit);
    if (afterIssuedAt == null || afterId == null) {
      // First page
      return jpaRepo.findFirstPage(tenantId, statusFilter, page);
    }
    // Subsequent pages
    return jpaRepo.findNextPage(tenantId, statusFilter, afterIssuedAt, afterId, page);
  }

  @Override
  public Optional<Invoice> findByInvoiceNumberAndTenant(String invoiceNumber, String tenantId) {
    return jpaRepo.findByInvoiceNumberAndTenantId(invoiceNumber, tenantId);
  }

  @Override
  public List<Invoice> findByOrderId(Long orderId) {
    return jpaRepo.findByOrderId(orderId);
  }
}
