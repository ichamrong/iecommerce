package com.chamrong.iecommerce.invoice.infrastructure;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link InvoiceRepository} port. */
@Repository
public interface JpaInvoiceRepository extends JpaRepository<Invoice, Long>, InvoiceRepository {
  @Override
  Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

  @Override
  List<Invoice> findByOrderId(Long orderId);

  @Override
  List<Invoice> findByTenantIdAndCreatedAtBetween(String tenantId, Instant start, Instant end);

  /**
   * Legacy idempotency lookup for older APIs.
   *
   * <p>The current schema does not persist an explicit idempotency key, so this implementation
   * always returns empty. Newer flows should rely on dedicated idempotency mechanisms instead.
   */
  @Override
  @Query("SELECT i FROM Invoice i WHERE 1 = 0")
  Optional<Invoice> findByIdempotencyKey(String idempotencyKey);
}
