package com.chamrong.iecommerce.invoice.application;

import com.chamrong.iecommerce.invoice.application.dto.InvoiceDetailResponse;
import com.chamrong.iecommerce.invoice.application.dto.InvoiceDetailResponse.InvoiceLineResponse;
import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceLine;
import com.chamrong.iecommerce.invoice.domain.InvoiceSignature;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Maps domain objects to API response DTOs.
 *
 * <p>Kept as a Spring component so it can be injected into both command handlers and query
 * handlers.
 */
@Component
public class InvoiceMapper {

  /**
   * Maps an {@link Invoice} and its optional {@link InvoiceSignature} to a full detail response.
   *
   * @param invoice the aggregate
   * @param signature the associated signature, or {@code null} for DRAFT invoices
   */
  public InvoiceDetailResponse toDetailResponse(Invoice invoice, InvoiceSignature signature) {
    List<InvoiceLineResponse> lineResponses =
        invoice.getLines().stream().map(this::toLineResponse).collect(Collectors.toList());

    return new InvoiceDetailResponse(
        invoice.getId(),
        invoice.getTenantId(),
        invoice.getInvoiceNumber(),
        invoice.getOrderId(),
        invoice.getCustomerId(),
        invoice.getStatus().name(),
        invoice.getCurrency(),
        invoice.getSubtotal(),
        invoice.getTaxAmount(),
        invoice.getTotal(),
        invoice.getIssueDate(),
        invoice.getDueDate(),
        invoice.getVoidReason(),
        invoice.getPaymentReference(),
        lineResponses,
        signature != null ? signature.getKeyId() : null,
        signature != null ? signature.getContentHash() : null,
        signature != null ? signature.getSignedAt() : null,
        invoice.getCreatedAt());
  }

  private InvoiceLineResponse toLineResponse(InvoiceLine line) {
    return new InvoiceLineResponse(
        line.getSku(),
        line.getProductName(),
        line.getDescription(),
        line.getQuantity(),
        line.getUnitPrice().getAmount(),
        line.getTaxRate(),
        line.getSubtotal().getAmount(),
        line.getLineOrder());
  }
}
