package com.chamrong.iecommerce.invoice.infrastructure;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link InvoiceRepository} port. */
@Repository
public interface JpaInvoiceRepository extends JpaRepository<Invoice, Long>, InvoiceRepository {
  @Override
  Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

  @Override
  List<Invoice> findByOrderId(Long orderId);
}
