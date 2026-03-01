package com.chamrong.iecommerce.invoice.infrastructure.pdf;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceLine;
import com.chamrong.iecommerce.invoice.domain.InvoiceSignature;
import com.chamrong.iecommerce.invoice.domain.port.InvoicePdfRendererPort;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * PDF renderer using OpenPDF (LGPL fork of iText 5).
 *
 * <p>Embeds:
 *
 * <ul>
 *   <li>Invoice header, line items table, totals
 *   <li>Footer: contentHash, keyId, signedAt
 *   <li>QR code: {@code {invoiceId}|{contentHash}|{signatureValue}|{keyId}}
 * </ul>
 */
@Slf4j
@Component
public class OpenPdfInvoiceRenderer implements InvoicePdfRendererPort {

  private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
  private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
  private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
  private static final Font FOOTER_FONT = FontFactory.getFont(FontFactory.COURIER, 7, Font.ITALIC);

  @Override
  public byte[] render(Invoice invoice, InvoiceSignature signature) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      Document doc = new Document();
      PdfWriter.getInstance(doc, baos);
      doc.open();

      // ── Title ──────────────────────────────────────────────────────────────
      Paragraph title = new Paragraph("INVOICE", TITLE_FONT);
      title.setAlignment(Element.ALIGN_CENTER);
      doc.add(title);
      doc.add(new Paragraph("\n"));

      // ── Invoice meta ───────────────────────────────────────────────────────
      doc.add(new Paragraph("Invoice #: " + nvl(invoice.getInvoiceNumber()), BODY_FONT));
      doc.add(new Paragraph("Status: " + invoice.getStatus(), BODY_FONT));
      doc.add(new Paragraph("Issue Date: " + nvl(invoice.getIssueDate()), BODY_FONT));
      doc.add(new Paragraph("Due Date: " + nvl(invoice.getDueDate()), BODY_FONT));
      doc.add(new Paragraph("Currency: " + invoice.getCurrency(), BODY_FONT));
      doc.add(new Paragraph("\n"));

      // ── Line items table ───────────────────────────────────────────────────
      PdfPTable table = new PdfPTable(new float[] {3f, 1f, 2f, 1.5f, 2f});
      table.setWidthPercentage(100);
      addHeaderCell(table, "Product");
      addHeaderCell(table, "Qty");
      addHeaderCell(table, "Unit Price");
      addHeaderCell(table, "Tax %");
      addHeaderCell(table, "Subtotal");

      for (InvoiceLine line : invoice.getLines()) {
        addCell(table, line.getProductName());
        addCell(table, String.valueOf(line.getQuantity()));
        addCell(table, line.getUnitPrice().getAmount().toPlainString());
        addCell(
            table,
            line.getTaxRate()
                    .multiply(java.math.BigDecimal.valueOf(100))
                    .setScale(2, java.math.RoundingMode.HALF_EVEN)
                    .toPlainString()
                + "%");
        addCell(table, line.getSubtotal().getAmount().toPlainString());
      }
      doc.add(table);
      doc.add(new Paragraph("\n"));

      // ── Totals ─────────────────────────────────────────────────────────────
      doc.add(new Paragraph("Subtotal: " + invoice.getSubtotal().toPlainString(), BODY_FONT));
      doc.add(new Paragraph("Tax: " + invoice.getTaxAmount().toPlainString(), BODY_FONT));
      Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
      doc.add(
          new Paragraph(
              "TOTAL: " + invoice.getTotal().toPlainString() + " " + invoice.getCurrency(),
              totalFont));
      doc.add(new Paragraph("\n\n"));

      // ── Signature footer ───────────────────────────────────────────────────
      doc.add(
          new Paragraph(
              "─── Digital Signature ───────────────────────────────────────────", FOOTER_FONT));
      doc.add(new Paragraph("Content Hash (SHA-256): " + signature.getContentHash(), FOOTER_FONT));
      doc.add(new Paragraph("Key ID: " + signature.getKeyId(), FOOTER_FONT));
      doc.add(new Paragraph("Algorithm: " + signature.getSignatureAlgorithm(), FOOTER_FONT));
      doc.add(new Paragraph("Signed At: " + signature.getSignedAt(), FOOTER_FONT));
      doc.add(new Paragraph("\n"));

      // ── QR code ────────────────────────────────────────────────────────────
      String qrContent =
          invoice.getId()
              + "|"
              + signature.getContentHash()
              + "|"
              + signature.getSignatureValue()
              + "|"
              + signature.getKeyId();
      byte[] qrBytes = generateQrCode(qrContent, 120);
      if (qrBytes != null) {
        Image qrImage = Image.getInstance(qrBytes);
        qrImage.setAlignment(Element.ALIGN_RIGHT);
        qrImage.scaleToFit(100, 100);
        doc.add(qrImage);
      }

      doc.close();
      return baos.toByteArray();
    } catch (Exception e) {
      throw new IllegalStateException("PDF generation failed for invoice " + invoice.getId(), e);
    }
  }

  private byte[] generateQrCode(String content, int size) {
    try {
      Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
      hints.put(EncodeHintType.MARGIN, 1);
      QRCodeWriter writer = new QRCodeWriter();
      BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
      ByteArrayOutputStream qrBaos = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(matrix, "PNG", qrBaos);
      return qrBaos.toByteArray();
    } catch (Exception e) {
      log.warn("QR code generation failed, PDF will be generated without QR", e);
      return null;
    }
  }

  private void addHeaderCell(PdfPTable table, String text) {
    PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
    cell.setBackgroundColor(new Color(200, 200, 200));
    cell.setPadding(4);
    table.addCell(cell);
  }

  private void addCell(PdfPTable table, String text) {
    PdfPCell cell = new PdfPCell(new Phrase(text, BODY_FONT));
    cell.setPadding(3);
    table.addCell(cell);
  }

  private String nvl(Object o) {
    return o != null ? o.toString() : "N/A";
  }
}
