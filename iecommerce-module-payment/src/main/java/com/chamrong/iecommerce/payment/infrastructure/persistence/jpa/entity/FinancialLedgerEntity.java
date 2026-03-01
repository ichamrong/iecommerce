package com.chamrong.iecommerce.payment.infrastructure.persistence.jpa.entity;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA Entity: Audit trail of all money movement (Payouts, Refunds, Platform commission).
 *
 * <p>This class carries @Entity and JPA annotations intentionally — it lives in {@code
 * infrastructure/persistence/jpa/entity/}, which is the correct location. The domain model for this
 * concept is covered by {@link com.chamrong.iecommerce.payment.domain.ledger.FinancialLedgerEntry}.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payment_financial_ledger")
public class FinancialLedgerEntity extends BaseTenantEntity {

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
  private String externalTransactionId;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  private Instant settledAt;

  @Column(columnDefinition = "TEXT")
  private String notes;

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
    SETTLED,
    REJECTED,
    REVERSED
  }

  // ── Factory ───────────────────────────────────────────────────────────────

  public static FinancialLedgerEntity of(
      String tenantId, Long orderId, LedgerType type, LedgerCategory category, Money amount) {
    var e = new FinancialLedgerEntity();
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

  public void settle(String externalTransactionId) {
    this.status = LedgerStatus.SETTLED;
    this.externalTransactionId = externalTransactionId;
    this.settledAt = Instant.now();
  }

  public void reject(String reason) {
    this.status = LedgerStatus.REJECTED;
    this.notes = reason;
  }

  public void reverse() {
    this.status = LedgerStatus.REVERSED;
  }
}
