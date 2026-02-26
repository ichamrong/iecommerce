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
import lombok.Getter;
import lombok.Setter;

/**
 * Audit trail of all money movement (Payouts to owners, Refunds to guests, Platform commission).
 * Supports manual execution oversight.
 */
@Getter
@Setter
@Entity
@Table(name = "payment_financial_ledger")
public class FinancialLedger extends BaseTenantEntity {

  @Column(nullable = false)
  private Long orderId; // Links to Booking/Order

  /** Target user: Customer for Refund, Owner for Payout. Null for Platform Commission. */
  @Column private Long destinationUserId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private LedgerType type; // IN, OUT

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private LedgerCategory category; // GUEST_PAYMENT, PLATFORM_COMMISSION, OWNER_PAYOUT, GUEST_REFUND

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  })
  private Money amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private LedgerStatus status = LedgerStatus.PENDING;

  /** Filled when the admin manually executes this transfer. */
  @Column(length = 255)
  private String adminReferenceId;

  /** Transaction ID directly from Bakong / Bank marking manual execution. */
  @Column(length = 100)
  private String bankTransactionId;

  public enum LedgerType {
    IN,
    OUT
  }

  public enum LedgerCategory {
    GUEST_PAYMENT,
    PLATFORM_COMMISSION,
    OWNER_PAYOUT,
    GUEST_REFUND,
    CANCELLATION_FEE
  }

  public enum LedgerStatus {
    PENDING,
    EXECUTED,
    FAILED
  }
}
