package com.chamrong.iecommerce.invoice.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.invoice.domain.exception.InvoiceImmutableException;
import com.chamrong.iecommerce.invoice.domain.exception.InvoiceTotalMismatchException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root for the Invoice bounded context.
 *
 * <h2>Invariants</h2>
 *
 * <ul>
 *   <li>Once {@code ISSUED}, only {@link #markPaid} or {@link #voidInvoice} are allowed.
 *   <li>{@code total = subtotal + taxAmount = sum(lineItem.subtotal + lineItem.taxAmount)}.
 *   <li>{@code invoiceNumber} is unique per tenant; assigned at issuance time.
 *   <li>Voiding requires a non-blank reason.
 *   <li>Must have at least one line item before issuance.
 * </ul>
 *
 * <p>No public setters — all mutation is through behavior methods.
 *
 * <p>ASVS V4.1 (access control): Tenant isolation enforced via {@link
 * BaseTenantEntity#getTenantId()}.
 */
@Entity
@Table(
    name = "invoice",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_invoice_tenant_number",
            columnNames = {"tenant_id", "invoice_number"}))
public class Invoice extends BaseTenantEntity {

  /** Required by JPA — not for application use. */
  protected Invoice() {}

  // ── Identity & References ─────────────────────────────────────────────────

  /** Tenant-scoped, sequential invoice number — assigned at issuance, null in DRAFT. */
  @Column(name = "invoice_number", length = 50)
  private String invoiceNumber;

  @Column(name = "order_id")
  private Long orderId;

  @Column(name = "customer_id")
  private Long customerId;

  // ── Dates ─────────────────────────────────────────────────────────────────

  /** Date the invoice was formally issued; null until {@link #issue} is called. */
  @Column(name = "issue_date")
  private Instant issueDate;

  /** Payment due date. Required before issuance. */
  @Column(name = "due_date")
  private LocalDate dueDate;

  // ── Monetary totals ───────────────────────────────────────────────────────

  /** ISO 4217 currency code for all monetary values on this invoice. */
  @Column(name = "currency", length = 3)
  private String currency;

  /** Sum of all line item subtotals (before tax). Recomputed on every save by the aggregate. */
  @Column(name = "subtotal", precision = 19, scale = 4)
  private BigDecimal subtotal = BigDecimal.ZERO;

  /** Sum of all line item tax amounts. Recomputed on every save. */
  @Column(name = "tax_amount", precision = 19, scale = 4)
  private BigDecimal taxAmount = BigDecimal.ZERO;

  /** Grand total = subtotal + taxAmount. Must equal sum of all line items at issuance. */
  @Column(name = "total", precision = 19, scale = 4)
  private BigDecimal total = BigDecimal.ZERO;

  // ── Status & lifecycle ────────────────────────────────────────────────────

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private InvoiceStatus status = InvoiceStatus.DRAFT;

  /** Mandatory reason when voiding — null for non-voided invoices. */
  @Column(name = "void_reason", columnDefinition = "TEXT")
  private String voidReason;

  /** Payment reference set when {@link #markPaid} is called. */
  @Column(name = "payment_reference", length = 255)
  private String paymentReference;

  // ── Immutable snapshot fields ─────────────────────────────────────────────
  // Stored as JSON to preserve historical accuracy even if seller/buyer data changes.

  /**
   * JSON snapshot of the seller (merchant) at invoice issuance time. Never updated after issuance.
   */
  @Column(name = "seller_snapshot", columnDefinition = "TEXT")
  private String sellerSnapshot;

  /**
   * JSON snapshot of the buyer (customer) at invoice issuance time. Never updated after issuance.
   */
  @Column(name = "buyer_snapshot", columnDefinition = "TEXT")
  private String buyerSnapshot;

  // ── Line items ────────────────────────────────────────────────────────────

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "invoice_id", nullable = false)
  @OrderBy("lineOrder ASC")
  private List<InvoiceLine> lines = new ArrayList<>();

  // ── Optimistic locking ────────────────────────────────────────────────────

  @Version
  @Column(nullable = false)
  private long version = 0L;

  // ── Factory ───────────────────────────────────────────────────────────────

  /**
   * Creates a new DRAFT invoice for the given tenant. No invoice number is assigned until {@link
   * #issue} is called.
   *
   * @param tenantId the owning tenant
   * @param orderId optional linked order ID
   * @param customerId optional customer ID
   * @param currency ISO 4217 currency code
   * @param dueDate payment due date
   * @param sellerSnapshot JSON of seller info
   * @param buyerSnapshot JSON of buyer info
   */
  public static Invoice createDraft(
      String tenantId,
      Long orderId,
      Long customerId,
      String currency,
      LocalDate dueDate,
      String sellerSnapshot,
      String buyerSnapshot) {
    Objects.requireNonNull(tenantId, "tenantId must not be null");
    Objects.requireNonNull(currency, "currency must not be null");

    Invoice invoice = new Invoice();
    invoice.setTenantId(tenantId);
    invoice.orderId = orderId;
    invoice.customerId = customerId;
    invoice.currency = currency.trim().toUpperCase();
    invoice.dueDate = dueDate;
    invoice.sellerSnapshot = sellerSnapshot;
    invoice.buyerSnapshot = buyerSnapshot;
    return invoice;
  }

  // ── Domain behaviour — DRAFT modifications ────────────────────────────────

  /**
   * Appends a line item to this DRAFT invoice and recomputes totals.
   *
   * @throws InvoiceImmutableException if the invoice is not in DRAFT status
   */
  public void addLine(InvoiceLine line) {
    Objects.requireNonNull(line, "line must not be null");
    if (!status.allowsModification()) {
      throw new InvoiceImmutableException(getId(), status, "addLine");
    }
    lines.add(line);
    recalculateTotals();
  }

  /**
   * Removes a line item from this DRAFT invoice and recomputes totals.
   *
   * @throws InvoiceImmutableException if the invoice is not in DRAFT status
   */
  public void removeLine(int lineOrder) {
    if (!status.allowsModification()) {
      throw new InvoiceImmutableException(getId(), status, "removeLine");
    }
    lines.removeIf(l -> l.getLineOrder() == lineOrder);
    recalculateTotals();
  }

  // ── Domain behaviour — lifecycle transitions ──────────────────────────────

  /**
   * Issues this invoice: assigns the invoice number, locks content, records issue date.
   *
   * <p>After this call the invoice is immutable except for {@link #markPaid} and {@link
   * #voidInvoice}.
   *
   * @param invoiceNumber unique, tenant-scoped number (generated by {@code
   *     InvoiceNumberGeneratorPort})
   * @param issuedAt clock-provided timestamp
   * @param sellerSnapshotJson up-to-date seller JSON snapshot (frozen at this moment)
   * @param buyerSnapshotJson up-to-date buyer JSON snapshot
   * @throws InvoiceImmutableException if not in DRAFT status
   * @throws IllegalStateException if there are no line items
   */
  public void issue(
      String invoiceNumber, Instant issuedAt, String sellerSnapshotJson, String buyerSnapshotJson) {
    if (status != InvoiceStatus.DRAFT) {
      throw new InvoiceImmutableException(getId(), status, "issue");
    }
    if (lines.isEmpty()) {
      throw new IllegalStateException("Cannot issue invoice " + getId() + ": no line items");
    }
    Objects.requireNonNull(invoiceNumber, "invoiceNumber must not be null");
    Objects.requireNonNull(issuedAt, "issuedAt must not be null");

    recalculateTotals();
    this.invoiceNumber = invoiceNumber;
    this.issueDate = issuedAt;
    this.sellerSnapshot = sellerSnapshotJson;
    this.buyerSnapshot = buyerSnapshotJson;
    this.status = InvoiceStatus.ISSUED;
  }

  /**
   * Voids this invoice with a mandatory reason.
   *
   * @param reason non-blank explanation (e.g., "Duplicate invoice", "Customer requested
   *     cancellation")
   * @param voidedAt timestamp
   * @throws InvoiceImmutableException if the current status does not allow voiding
   * @throws IllegalArgumentException if reason is blank
   */
  public void voidInvoice(String reason, Instant voidedAt) {
    if (!status.allowsVoid()) {
      throw new InvoiceImmutableException(getId(), status, "void");
    }
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("Void reason must not be blank");
    }
    this.voidReason = reason;
    this.status = InvoiceStatus.VOIDED;
  }

  /**
   * Records payment receipt.
   *
   * @param paymentReference external payment ID / transaction reference
   * @param paidAt timestamp
   * @throws InvoiceImmutableException if the current status does not allow payment
   */
  public void markPaid(String paymentReference, Instant paidAt) {
    if (!status.allowsPaid()) {
      throw new InvoiceImmutableException(getId(), status, "markPaid");
    }
    this.paymentReference = paymentReference;
    this.status = InvoiceStatus.PAID;
  }

  /**
   * Transitions ISSUED → OVERDUE (called by a scheduled job when due date has passed).
   *
   * @throws InvoiceImmutableException if not in ISSUED status
   */
  public void flagOverdue() {
    if (status != InvoiceStatus.ISSUED) {
      throw new InvoiceImmutableException(getId(), status, "flagOverdue");
    }
    this.status = InvoiceStatus.OVERDUE;
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  /**
   * Recomputes {@code subtotal}, {@code taxAmount}, and {@code total} from all current line items.
   *
   * <p>Uses {@link Money#ROUNDING} for all intermediate sums, ensuring deterministic results.
   *
   * @throws InvoiceTotalMismatchException if computed total != declared total (sanity check after
   *     manual override — normally never thrown here since totals are always derived)
   */
  public void recalculateTotals() {
    if (lines.isEmpty()) {
      this.subtotal = BigDecimal.ZERO;
      this.taxAmount = BigDecimal.ZERO;
      this.total = BigDecimal.ZERO;
      return;
    }

    Money runningSubtotal = Money.zero(currency);
    Money runningTax = Money.zero(currency);

    for (InvoiceLine line : lines) {
      runningSubtotal = runningSubtotal.add(line.computeSubtotal());
      runningTax = runningTax.add(line.computeTaxAmount());
    }

    this.subtotal = runningSubtotal.getAmount();
    this.taxAmount = runningTax.getAmount();
    this.total = runningSubtotal.add(runningTax).getAmount();
  }

  // ── Accessors ──────────────────────────────────────────────────────────────

  public String getInvoiceNumber() {
    return invoiceNumber;
  }

  public Long getOrderId() {
    return orderId;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public Instant getIssueDate() {
    return issueDate;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public String getCurrency() {
    return currency;
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public BigDecimal getTaxAmount() {
    return taxAmount;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public InvoiceStatus getStatus() {
    return status;
  }

  public String getVoidReason() {
    return voidReason;
  }

  public String getPaymentReference() {
    return paymentReference;
  }

  public String getSellerSnapshot() {
    return sellerSnapshot;
  }

  public String getBuyerSnapshot() {
    return buyerSnapshot;
  }

  /**
   * @return an unmodifiable view of the line items.
   */
  public List<InvoiceLine> getLines() {
    return Collections.unmodifiableList(lines);
  }

  public long getVersion() {
    return version;
  }
}
