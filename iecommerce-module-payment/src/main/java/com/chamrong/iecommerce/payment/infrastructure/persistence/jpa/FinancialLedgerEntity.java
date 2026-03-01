package com.chamrong.iecommerce.payment.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA projection of the financial ledger (payment infrastructure sub-package). */
@Entity
@Table(name = "financial_ledger")
public class FinancialLedgerEntity {

  public FinancialLedgerEntity() {}

  public enum EntryType {
    DEBIT,
    CREDIT,
    REFUND,
    CHARGEBACK,
    FEE
  }

  public enum LedgerStatus {
    PENDING,
    SETTLED,
    FAILED,
    REVERSED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 100)
  private String tenantId;

  @Column(nullable = false)
  private Long orderId;

  @Column(nullable = false)
  private Long paymentIntentId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EntryType entryType;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private LedgerStatus status;

  @Column(length = 100)
  private String externalReference;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  private Instant settledAt;

  @Column(columnDefinition = "TEXT")
  private String description;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Long getPaymentIntentId() {
    return paymentIntentId;
  }

  public void setPaymentIntentId(Long paymentIntentId) {
    this.paymentIntentId = paymentIntentId;
  }

  public EntryType getEntryType() {
    return entryType;
  }

  public void setEntryType(EntryType entryType) {
    this.entryType = entryType;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public LedgerStatus getStatus() {
    return status;
  }

  public void setStatus(LedgerStatus status) {
    this.status = status;
  }

  public String getExternalReference() {
    return externalReference;
  }

  public void setExternalReference(String externalReference) {
    this.externalReference = externalReference;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getSettledAt() {
    return settledAt;
  }

  public void setSettledAt(Instant settledAt) {
    this.settledAt = settledAt;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  // ── Factory ───────────────────────────────────────────────────────────────

  public static FinancialLedgerEntity of(
      String tenantId,
      Long orderId,
      Long paymentIntentId,
      EntryType entryType,
      BigDecimal amount,
      String currency) {
    var e = new FinancialLedgerEntity();
    e.tenantId = tenantId;
    e.orderId = orderId;
    e.paymentIntentId = paymentIntentId;
    e.entryType = entryType;
    e.amount = amount;
    e.currency = currency;
    e.status = LedgerStatus.PENDING;
    e.createdAt = Instant.now();
    return e;
  }

  // ── Domain behaviour ─────────────────────────────────────────────────────

  public void settle(String externalReference) {
    this.status = LedgerStatus.SETTLED;
    this.settledAt = Instant.now();
    this.externalReference = externalReference;
  }

  public void fail() {
    this.status = LedgerStatus.FAILED;
  }

  public void reverse() {
    this.status = LedgerStatus.REVERSED;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FinancialLedgerEntity that)) return false;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
