package com.chamrong.iecommerce.invoice.infrastructure.persistence;

import com.chamrong.iecommerce.invoice.domain.InvoiceSignature;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceSignatureRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for {@link InvoiceSignature}. */
@Repository
interface SpringDataInvoiceSignatureRepository extends JpaRepository<InvoiceSignature, Long> {
  Optional<InvoiceSignature> findByInvoiceId(Long invoiceId);
}

/** Adapter: implements {@link InvoiceSignatureRepositoryPort} via JPA. */
@Component
@RequiredArgsConstructor
class JpaInvoiceSignatureAdapter implements InvoiceSignatureRepositoryPort {

  private final SpringDataInvoiceSignatureRepository jpaRepo;

  @Override
  public InvoiceSignature save(InvoiceSignature signature) {
    return jpaRepo.save(signature);
  }

  @Override
  public Optional<InvoiceSignature> findByInvoiceId(Long invoiceId) {
    return jpaRepo.findByInvoiceId(invoiceId);
  }
}
