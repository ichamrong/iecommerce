package com.chamrong.iecommerce.invoice.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.invoice.domain.InvoiceSignature;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceSignatureRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
