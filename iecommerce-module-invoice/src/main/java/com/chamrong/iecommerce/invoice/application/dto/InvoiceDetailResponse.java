package com.chamrong.iecommerce.invoice.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Full detail view of a single invoice — returned by GetInvoiceDetail and mutation commands.
 *
 * @param id invoice primary key
 * @param tenantId owning tenant
 * @param invoiceNumber assigned number (null while DRAFT)
 * @param orderId linked order
 * @param customerId linked customer
 * @param status current lifecycle status
 * @param currency ISO 4217 currency
 * @param subtotal sum of line subtotals
 * @param taxAmount sum of line taxes
 * @param total grand total
 * @param issueDate when issued (null while DRAFT)
 * @param dueDate payment due date
 * @param voidReason null unless VOIDED
 * @param paymentReference null unless PAID
 * @param lines line items
 * @param signatureKeyId keyId of the signature (null while DRAFT)
 * @param contentHash SHA-256 of canonical payload (null while DRAFT)
 * @param signedAt when signature was computed (null while DRAFT)
 * @param createdAt DB creation timestamp
 */
public record InvoiceDetailResponse(
    Long id,
    String tenantId,
    String invoiceNumber,
    Long orderId,
    Long customerId,
    String status,
    String currency,
    BigDecimal subtotal,
    BigDecimal taxAmount,
    BigDecimal total,
    Instant issueDate,
    LocalDate dueDate,
    String voidReason,
    String paymentReference,
    List<InvoiceLineResponse> lines,
    String signatureKeyId,
    String contentHash,
    Instant signedAt,
    Instant createdAt) {

  /** Compact line-item read model. */
  public record InvoiceLineResponse(
      String sku,
      String productName,
      String description,
      int quantity,
      BigDecimal unitPrice,
      BigDecimal taxRate,
      BigDecimal subtotal,
      int lineOrder) {}
}
