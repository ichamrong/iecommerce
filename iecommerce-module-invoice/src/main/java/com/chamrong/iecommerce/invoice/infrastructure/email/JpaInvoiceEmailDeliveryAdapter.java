package com.chamrong.iecommerce.invoice.infrastructure.email;

import com.chamrong.iecommerce.invoice.domain.port.InvoiceEmailDeliveryPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA interface for {@link InvoiceEmailDelivery}.
 *
 * <p>Package-private — only accessed via {@link JpaInvoiceEmailDeliveryAdapter}.
 */
@Repository
interface SpringDataInvoiceEmailDeliveryRepository
    extends JpaRepository<InvoiceEmailDelivery, Long> {

  Optional<InvoiceEmailDelivery> findByIdempotencyKey(String idempotencyKey);

  Optional<InvoiceEmailDelivery> findByInvoiceIdAndMessageType(
      Long invoiceId, InvoiceEmailDelivery.MessageType messageType);

  @Query(
      "SELECT d FROM InvoiceEmailDelivery d "
          + "WHERE d.status = 'PENDING' "
          + "AND (d.lastAttemptedAt IS NULL OR d.lastAttemptedAt < :cutoff) "
          + "ORDER BY d.createdAt ASC")
  List<InvoiceEmailDelivery> findRetryable(@Param("cutoff") Instant cutoff);
}

/** Public JPA adapter implementing {@link InvoiceEmailDeliveryPort}. */
@Component
class JpaInvoiceEmailDeliveryAdapter implements InvoiceEmailDeliveryPort {

  private final SpringDataInvoiceEmailDeliveryRepository jpaRepo;

  JpaInvoiceEmailDeliveryAdapter(SpringDataInvoiceEmailDeliveryRepository jpaRepo) {
    this.jpaRepo = jpaRepo;
  }

  @Override
  public InvoiceEmailDelivery save(InvoiceEmailDelivery delivery) {
    return jpaRepo.save(delivery);
  }

  @Override
  public Optional<InvoiceEmailDelivery> findByIdempotencyKey(String idempotencyKey) {
    return jpaRepo.findByIdempotencyKey(idempotencyKey);
  }

  @Override
  public Optional<InvoiceEmailDelivery> findByInvoiceIdAndMessageType(
      Long invoiceId, InvoiceEmailDelivery.MessageType messageType) {
    return jpaRepo.findByInvoiceIdAndMessageType(invoiceId, messageType);
  }

  @Override
  public List<InvoiceEmailDelivery> findRetryable(Instant cutoff) {
    return jpaRepo.findRetryable(cutoff);
  }
}
