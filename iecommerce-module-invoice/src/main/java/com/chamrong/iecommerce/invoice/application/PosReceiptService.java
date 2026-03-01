package com.chamrong.iecommerce.invoice.application;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Handles POS specific receipt generation including formatting for thermal printers. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PosReceiptService {

  private final InvoiceRepository invoiceRepository;

  @Transactional(readOnly = true)
  public String generateThermalReceipt(String tenantId, Long invoiceId, Long terminalId) {
    Invoice invoice =
        invoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

    if (!invoice.getTenantId().equals(tenantId)) {
      throw new IllegalArgumentException("Invalid invoice access");
    }

    StringBuilder receipt = new StringBuilder();
    receipt.append("--------------------------------\n");
    receipt.append("          RECEIPT               \n");
    receipt.append("--------------------------------\n");
    receipt.append("Terminal: ").append(terminalId).append("\n");
    receipt.append("Invoice #: ").append(invoice.getInvoiceNumber()).append("\n");
    // Use issueDate (replaces legacy invoiceDate field)
    receipt.append("Date: ").append(invoice.getIssueDate()).append("\n");
    receipt.append("--------------------------------\n");

    invoice
        .getLines()
        .forEach(
            line -> {
              receipt.append(line.getProductName()).append("\n");
              java.math.BigDecimal lineTotal =
                  line.getUnitPrice()
                      .getAmount()
                      .multiply(java.math.BigDecimal.valueOf(line.getQuantity()));

              receipt.append(
                  String.format(
                      "%d x %.2f %s   %.2f\n",
                      line.getQuantity(),
                      line.getUnitPrice().getAmount(),
                      line.getUnitPrice().getCurrency(),
                      lineTotal));
            });

    receipt.append("--------------------------------\n");
    // Use getTotal() / getCurrency() — replaces legacy getTotalAmount()
    receipt.append(String.format("TOTAL: %.2f %s\n", invoice.getTotal(), invoice.getCurrency()));
    receipt.append("--------------------------------\n");
    receipt.append("       THANK YOU!               \n");
    receipt.append("--------------------------------\n");

    log.info("Generated thermal receipt for invoice {} at terminal {}", invoiceId, terminalId);
    return receipt.toString();
  }
}
