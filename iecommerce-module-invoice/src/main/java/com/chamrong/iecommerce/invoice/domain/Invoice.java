package com.chamrong.iecommerce.invoice.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice")
public class Invoice extends BaseTenantEntity {

  public Invoice() {}

  @Column(unique = true, nullable = false, length = 50)
  private String invoiceNumber;

  @Column(nullable = false)
  private Long orderId;

  @Column(nullable = false)
  private Instant invoiceDate = Instant.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private InvoiceStatus status = InvoiceStatus.DRAFT;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  })
  private Money totalAmount;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "invoice_id")
  private List<InvoiceLine> lines = new ArrayList<>();

  @Column(columnDefinition = "TEXT")
  private String digitalSignature;

  private Instant signedAt;

  @Column(unique = true, length = 100)
  private String idempotencyKey;

  public String getInvoiceNumber() {
    return invoiceNumber;
  }

  public void setInvoiceNumber(String invoiceNumber) {
    this.invoiceNumber = invoiceNumber;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Instant getInvoiceDate() {
    return invoiceDate;
  }

  public void setInvoiceDate(Instant invoiceDate) {
    this.invoiceDate = invoiceDate;
  }

  public InvoiceStatus getStatus() {
    return status;
  }

  public void setStatus(InvoiceStatus status) {
    this.status = status;
  }

  public Money getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(Money totalAmount) {
    this.totalAmount = totalAmount;
  }

  public List<InvoiceLine> getLines() {
    return lines;
  }

  public void setLines(List<InvoiceLine> lines) {
    this.lines = lines;
  }

  public String getDigitalSignature() {
    return digitalSignature;
  }

  public void setDigitalSignature(String digitalSignature) {
    this.digitalSignature = digitalSignature;
  }

  public Instant getSignedAt() {
    return signedAt;
  }

  public void setSignedAt(Instant signedAt) {
    this.signedAt = signedAt;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  // ── Domain behaviour ───────────────────────────────────────────────────────

  public void issue() {
    if (this.status != InvoiceStatus.DRAFT) {
      throw new IllegalStateException("Only DRAFT invoices can be issued");
    }
    this.status = InvoiceStatus.ISSUED;
    this.invoiceDate = Instant.now();
  }

  public void markPaid() {
    this.status = InvoiceStatus.PAID;
  }

  public void void_() {
    if (this.status == InvoiceStatus.PAID) {
      throw new IllegalStateException("Cannot void a PAID invoice");
    }
    this.status = InvoiceStatus.VOID;
  }

  /** Write-once setter — idempotency key is set once at creation time. */
  public void setIdempotencyKey(String idempotencyKey) {
    if (this.idempotencyKey != null) throw new IllegalStateException("idempotencyKey already set");
    this.idempotencyKey = idempotencyKey;
  }
}
