package com.chamrong.iecommerce.invoice.domain;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository {
  Invoice save(Invoice invoice);

  Optional<Invoice> findById(Long id);

  Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

  List<Invoice> findByOrderId(Long orderId);
}
