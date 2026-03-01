package com.chamrong.iecommerce.invoice.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceLine;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Tests that InvoiceCanonicalizer produces deterministic, identical output for the same input. */
class InvoiceCanonicalizerTest {

  private final InvoiceCanonicalizer canonicalizer = new InvoiceCanonicalizer();

  @Test
  void sameInvoice_producesSameBytes() {
    Invoice invoice = buildIssuedInvoice();
    byte[] first = canonicalizer.canonicalize(invoice);
    byte[] second = canonicalizer.canonicalize(invoice);
    assertThat(first).isEqualTo(second);
  }

  @Test
  void sha256_produces64CharHexString() {
    byte[] bytes = "test-canonical-payload".getBytes();
    String hash = canonicalizer.sha256Hex(bytes);
    assertThat(hash).hasSize(64).matches("[0-9a-f]+");
  }

  @Test
  void differentContent_producesDifferentHash() {
    byte[] a = "invoice-a".getBytes();
    byte[] b = "invoice-b".getBytes();
    assertThat(canonicalizer.sha256Hex(a)).isNotEqualTo(canonicalizer.sha256Hex(b));
  }

  private static Invoice buildIssuedInvoice() {
    Invoice invoice =
        Invoice.createDraft(
            "tenant-1",
            42L,
            99L,
            "USD",
            LocalDate.of(2026, 12, 31),
            "{\"name\":\"Seller Ltd\"}",
            "{\"name\":\"Buyer Corp\"}");
    invoice.addLine(
        InvoiceLine.of(
            "SKU-001",
            "Product A",
            "Description",
            3,
            new Money(new BigDecimal("100.00"), "USD"),
            new BigDecimal("0.10"),
            0));
    invoice.issue(
        "TENANT1-2026-000001",
        Instant.parse("2026-01-15T12:00:00Z"),
        "{\"name\":\"Seller Ltd\"}",
        "{\"name\":\"Buyer Corp\"}");
    return invoice;
  }
}
