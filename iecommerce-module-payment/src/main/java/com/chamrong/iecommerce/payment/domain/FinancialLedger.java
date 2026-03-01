package com.chamrong.iecommerce.payment.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "payment_financial_ledger")
public class FinancialLedger extends BaseTenantEntity {

  public FinancialLedger() {}

  @Column(nullable = false)
  private Long orderId;

  @Column private Long destinationUserId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private LedgerType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private LedgerCategory category;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  })
  private Money amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private LedgerStatus status = LedgerStatus.PENDING;

  @Column(length = 255)
  private String adminReferenceId;

  @Column(length = 255)
  private String bankTransactionId;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  private Instant settledAt;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column private Boolean manualOverride;
  @Column private String manualOverrideReason;
  @Column private String approvedByAdmin;

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Long getDestinationUserId() {
    return destinationUserId;
  }

  public void setDestinationUserId(Long destinationUserId) {
    this.destinationUserId = destinationUserId;
  }

  public LedgerType getType() {
    return type;
  }

  public void setType(LedgerType type) {
    this.type = type;
  }

  public LedgerCategory getCategory() {
    return category;
  }

  public void setCategory(LedgerCategory category) {
    this.category = category;
  }

  public Money getAmount() {
    return amount;
  }

  public void setAmount(Money amount) {
    this.amount = amount;
  }

  public LedgerStatus getStatus() {
    return status;
  }

  public void setStatus(LedgerStatus status) {
    this.status = status;
  }

  public String getAdminReferenceId() {
    return adminReferenceId;
  }

  public void setAdminReferenceId(String adminReferenceId) {
    this.adminReferenceId = adminReferenceId;
  }

  public String getBankTransactionId() {
    return bankTransactionId;
  }

  public void setBankTransactionId(String bankTransactionId) {
    this.bankTransactionId = bankTransactionId;
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

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Boolean getManualOverride() {
    return manualOverride;
  }

  public void setManualOverride(Boolean manualOverride) {
    this.manualOverride = manualOverride;
  }

  public String getManualOverrideReason() {
    return manualOverrideReason;
  }

  public void setManualOverrideReason(String manualOverrideReason) {
    this.manualOverrideReason = manualOverrideReason;
  }

  public String getApprovedByAdmin() {
    return approvedByAdmin;
  }

  public void setApprovedByAdmin(String approvedByAdmin) {
    this.approvedByAdmin = approvedByAdmin;
  }

  public enum LedgerType {
    PAYOUT,
    REFUND,
    COMMISSION,
    ADJUSTMENT,
    REVERSAL
  }

  public enum LedgerCategory {
    ACCOMMODATION,
    SERVICE,
    PLATFORM_FEE,
    TAX,
    DISCOUNT
  }

  public enum LedgerStatus {
    PENDING,
    APPROVED,
    EXECUTED,
    SETTLED,
    REJECTED,
    REVERSED
  }

  // ── Factory ───────────────────────────────────────────────────────────────

  public static FinancialLedger of(
      String tenantId, Long orderId, LedgerType type, LedgerCategory category, Money amount) {
    var e = new FinancialLedger();
    e.setTenantId(tenantId);
    e.orderId = orderId;
    e.type = type;
    e.category = category;
    e.amount = amount;
    e.status = LedgerStatus.PENDING;
    e.createdAt = Instant.now();
    return e;
  }

  // ── Domain behaviour ─────────────────────────────────────────────────────

  public void approve() {
    this.status = LedgerStatus.APPROVED;
  }

  public void settle(String bankTransactionId) {
    this.status = LedgerStatus.SETTLED;
    this.bankTransactionId = bankTransactionId;
    this.settledAt = Instant.now();
  }

  public void reject(String reason) {
    this.status = LedgerStatus.REJECTED;
    this.notes = reason;
  }

  public void reverse() {
    this.status = LedgerStatus.REVERSED;
  }

  public void applyManualOverride(String reason, String adminId) {
    this.manualOverride = true;
    this.manualOverrideReason = reason;
    this.approvedByAdmin = adminId;
  }
}
