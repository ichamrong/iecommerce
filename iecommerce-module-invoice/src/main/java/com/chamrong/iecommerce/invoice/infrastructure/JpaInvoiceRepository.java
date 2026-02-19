package com.chamrong.iecommerce.invoice.infrastructure;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaInvoiceRepository implements InvoiceRepository {

  private final InvoiceJpaInterface jpaInterface;

  public JpaInvoiceRepository(InvoiceJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public Invoice save(Invoice invoice) {
    return jpaInterface.save(invoice);
  }

  @Override
  public Optional<Invoice> findById(Long id) {
    return jpaInterface.findById(id);
  }

  @Override
  public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
    return jpaInterface.findByInvoiceNumber(invoiceNumber);
  }

  @Override
  public List<Invoice> findByOrderId(Long orderId) {
    return jpaInterface.findByOrderId(orderId);
  }

  public interface InvoiceJpaInterface extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByOrderId(Long orderId);
  }
}
