package com.chamrong.iecommerce.invoice.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.invoice.domain.InvoiceSignature;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for {@link InvoiceSignature}. */
@Repository
interface SpringDataInvoiceSignatureRepository extends JpaRepository<InvoiceSignature, Long> {
  Optional<InvoiceSignature> findByInvoiceId(Long invoiceId);
}
