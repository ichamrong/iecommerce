package com.chamrong.iecommerce.invoice.infrastructure.security;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceLine;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Produces a deterministic canonical byte representation of an {@link Invoice} payload for signing
 * and hash computation.
 *
 * <h2>Canonicalization Rules</h2>
 *
 * <ul>
 *   <li>Keys are sorted alphabetically at every nesting level.
 *   <li>Null values are excluded.
 *   <li>Line items are sorted by {@code lineOrder} before serialization.
 *   <li>Monetary amounts are serialized as plain decimal strings (no scientific notation).
 *   <li>Timestamps serialized as ISO-8601 strings.
 *   <li>Output charset: UTF-8.
 * </ul>
 *
 * <p>These rules ensure the same invoice always produces the same byte sequence regardless of JVM
 * version, Jackson version, or field insertion order.
 */
@Component
public class InvoiceCanonicalizer {

  private static final ObjectMapper CANONICAL_MAPPER = buildCanonicalMapper();

  private static ObjectMapper buildCanonicalMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    // Sort map keys and bean properties alphabetically — critical for determinism
    mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    mapper.configure(
        com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    // Exclude nulls
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper;
  }

  /**
   * Produces a canonical UTF-8 byte array for the given invoice.
   *
   * <p>Only fields that affect the invoice's financial integrity are included: invoiceNumber,
   * issuedAt, currency, subtotal, taxAmount, total, lineItems.
   *
   * @param invoice the issued invoice to canonicalize
   * @return canonical UTF-8 bytes
   */
  public byte[] canonicalize(Invoice invoice) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("buyerSnapshot", invoice.getBuyerSnapshot());
    payload.put("currency", invoice.getCurrency());
    payload.put("customerId", invoice.getCustomerId());
    payload.put("dueDate", invoice.getDueDate() != null ? invoice.getDueDate().toString() : null);
    payload.put("invoiceId", invoice.getId());
    payload.put("invoiceNumber", invoice.getInvoiceNumber());
    payload.put(
        "issueDate", invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : null);
    payload.put("lines", canonicalizeLines(invoice.getLines()));
    payload.put("orderId", invoice.getOrderId());
    payload.put("sellerSnapshot", invoice.getSellerSnapshot());
    payload.put(
        "subtotal", invoice.getSubtotal() != null ? invoice.getSubtotal().toPlainString() : null);
    payload.put(
        "taxAmount",
        invoice.getTaxAmount() != null ? invoice.getTaxAmount().toPlainString() : null);
    payload.put("tenantId", invoice.getTenantId());
    payload.put("total", invoice.getTotal() != null ? invoice.getTotal().toPlainString() : null);

    // Remove null values (already excluded by mapper, but keep map clean)
    payload.values().removeIf(v -> v == null);

    try {
      String json = CANONICAL_MAPPER.writeValueAsString(payload);
      return json.getBytes(StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to canonicalize invoice " + invoice.getId(), e);
    }
  }

  /**
   * Computes the SHA-256 hex digest of pre-computed canonical bytes.
   *
   * @param canonical bytes from {@link #canonicalize(Invoice)}
   * @return lowercase hex SHA-256, 64 chars
   */
  public String sha256Hex(byte[] canonical) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(canonical);
      StringBuilder sb = new StringBuilder(64);
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private List<Map<String, Object>> canonicalizeLines(List<InvoiceLine> lines) {
    return lines.stream()
        .sorted(Comparator.comparingInt(InvoiceLine::getLineOrder))
        .map(
            line -> {
              Map<String, Object> m = new LinkedHashMap<>();
              m.put("description", line.getDescription());
              m.put("lineOrder", line.getLineOrder());
              m.put("productName", line.getProductName());
              m.put("quantity", line.getQuantity());
              m.put("sku", line.getSku());
              m.put(
                  "subtotal",
                  toPlainString(
                      line.getSubtotal() != null ? line.getSubtotal().getAmount() : null));
              m.put(
                  "taxRate", line.getTaxRate() != null ? line.getTaxRate().toPlainString() : null);
              m.put(
                  "unitPrice",
                  toPlainString(
                      line.getUnitPrice() != null ? line.getUnitPrice().getAmount() : null));
              m.values().removeIf(v -> v == null);
              return m;
            })
        .collect(Collectors.toList());
  }

  private String toPlainString(BigDecimal value) {
    return value != null ? value.toPlainString() : null;
  }
}
