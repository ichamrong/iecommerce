package com.chamrong.iecommerce.invoice.application;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceService {

  private final InvoiceRepository invoiceRepository;

  public InvoiceService(InvoiceRepository invoiceRepository) {
    this.invoiceRepository = invoiceRepository;
  }

  @Transactional
  public Invoice createInvoice(Invoice invoice) {
    return invoiceRepository.save(invoice);
  }

  public Optional<Invoice> getInvoiceById(Long id) {
    return invoiceRepository.findById(id);
  }

  public List<Invoice> getInvoicesByOrderId(Long orderId) {
    return invoiceRepository.findByOrderId(orderId);
  }
}
