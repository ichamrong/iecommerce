package com.chamrong.iecommerce.invoice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.invoice.domain.exception.InvoiceImmutableException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Invoice} aggregate root.
 *
 * <p>Covers invariants, lifecycle state machine, and total computation without any Spring context.
 */
class InvoiceAggregateTest {

  private static final String TENANT = "tenant-1";

  @Test
  void createDraft_setsStatusToOraft() {
    Invoice invoice = buildDraft();
    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
    assertThat(invoice.getInvoiceNumber()).isNull();
    assertThat(invoice.getIssueDate()).isNull();
  }

  @Test
  void addLine_recalculatesTotals() {
    Invoice invoice = buildDraft();
    invoice.addLine(buildLine("P1", 2, "100.00", "0.10", 0));

    // subtotal = 2 * 100 = 200, tax = 200 * 0.10 = 20, total = 220
    assertThat(invoice.getSubtotal()).isEqualByComparingTo("200.0000");
    assertThat(invoice.getTaxAmount()).isEqualByComparingTo("20.0000");
    assertThat(invoice.getTotal()).isEqualByComparingTo("220.0000");
  }

  @Test
  void addLine_afterIssue_throwsInvoiceImmutableException() {
    Invoice invoice = buildDraft();
    invoice.addLine(buildLine("P1", 1, "50.00", "0.00", 0));
    issue(invoice);

    assertThatThrownBy(() -> invoice.addLine(buildLine("P2", 1, "10.00", "0.00", 1)))
        .isInstanceOf(InvoiceImmutableException.class)
        .hasMessageContaining("ISSUED");
  }

  @Test
  void issue_setsStatusAndNumber() {
    Invoice invoice = buildDraft();
    invoice.addLine(buildLine("P1", 1, "50.00", "0.00", 0));
    issue(invoice);

    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
    assertThat(invoice.getInvoiceNumber()).isEqualTo("INV-2026-000001");
    assertThat(invoice.getIssueDate()).isNotNull();
  }

  @Test
  void issue_withNoLines_throwsIllegalState() {
    Invoice invoice = buildDraft();

    assertThatThrownBy(() -> issue(invoice))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("no line items");
  }

  @Test
  void voidInvoice_afterIssue_setsStatusAndReason() {
    Invoice invoice = buildDraft();
    invoice.addLine(buildLine("P1", 1, "50.00", "0.00", 0));
    issue(invoice);

    invoice.voidInvoice("Customer requested cancellation", Instant.now());

    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.VOIDED);
    assertThat(invoice.getVoidReason()).isEqualTo("Customer requested cancellation");
  }

  @Test
  void voidInvoice_withBlankReason_throwsIllegalArgument() {
    Invoice invoice = buildDraft();
    invoice.addLine(buildLine("P1", 1, "50.00", "0.00", 0));
    issue(invoice);

    assertThatThrownBy(() -> invoice.voidInvoice("  ", Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("blank");
  }

  @Test
  void markPaid_afterIssue_transitionsToPaid() {
    Invoice invoice = buildDraft();
    invoice.addLine(buildLine("P1", 1, "50.00", "0.00", 0));
    issue(invoice);

    invoice.markPaid("TXN-123", Instant.now());

    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    assertThat(invoice.getPaymentReference()).isEqualTo("TXN-123");
  }

  @Test
  void markPaid_onDraft_throwsImmutableException() {
    Invoice invoice = buildDraft();

    assertThatThrownBy(() -> invoice.markPaid("TXN-123", Instant.now()))
        .isInstanceOf(InvoiceImmutableException.class);
  }

  @Test
  void flagOverdue_afterIssue_transitionsToOverdue() {
    Invoice invoice = buildDraft();
    invoice.addLine(buildLine("P1", 1, "50.00", "0.00", 0));
    issue(invoice);
    invoice.flagOverdue();

    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OVERDUE);
  }

  @Test
  void multipleLines_totalsAreSum() {
    Invoice invoice = buildDraft();
    invoice.addLine(buildLine("P1", 2, "100.00", "0.10", 0)); // 200 + 20 tax = 220
    invoice.addLine(buildLine("P2", 3, "50.00", "0.05", 1)); // 150 + 7.5 tax = 157.5

    // subtotal = 350, tax = 27.5, total = 377.5
    assertThat(invoice.getSubtotal()).isEqualByComparingTo("350.0000");
    assertThat(invoice.getTaxAmount()).isEqualByComparingTo("27.5000");
    assertThat(invoice.getTotal()).isEqualByComparingTo("377.5000");
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private static Invoice buildDraft() {
    return Invoice.createDraft(
        TENANT,
        null,
        null,
        "USD",
        LocalDate.of(2026, 12, 31),
        "{\"name\":\"Seller\"}",
        "{\"name\":\"Buyer\"}");
  }

  private static void issue(Invoice invoice) {
    invoice.issue(
        "INV-2026-000001", Instant.now(), "{\"name\":\"Seller\"}", "{\"name\":\"Buyer\"}");
  }

  private static InvoiceLine buildLine(
      String product, int qty, String unitPrice, String taxRate, int lineOrder) {
    return InvoiceLine.of(
        null,
        product,
        null,
        qty,
        new Money(new BigDecimal(unitPrice), "USD"),
        new BigDecimal(taxRate),
        lineOrder);
  }
}
