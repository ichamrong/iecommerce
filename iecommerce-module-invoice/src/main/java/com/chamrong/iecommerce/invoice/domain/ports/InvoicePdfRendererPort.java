package com.chamrong.iecommerce.invoice.domain.ports;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceSignature;

/**
 * Output port: generates a PDF document for an invoice, embedding signature metadata.
 *
 * <p>PDF layout must include:
 *
 * <ul>
 *   <li>Invoice header (number, dates, seller/buyer snapshots)
 *   <li>Line item table
 *   <li>Totals block (subtotal, tax, total)
 *   <li>Footer with: {@code contentHash}, {@code keyId}, {@code signedAt}
 *   <li>QR code encoding: {@code {invoiceId}|{contentHash}|{signatureValue}|{keyId}}
 * </ul>
 */
public interface InvoicePdfRendererPort {

  /**
   * Renders the invoice as a PDF byte array.
   *
   * @param invoice the invoice aggregate (must be ISSUED, PAID, or VOIDED)
   * @param signature the associated signature — required for embedding in the footer and QR code
   * @return raw PDF bytes suitable for streaming to the client
   */
  byte[] render(Invoice invoice, InvoiceSignature signature);
}
