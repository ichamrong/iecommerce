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
import jakarta.persistence.Version;
import lombok.Getter;

@Getter
@Entity
@Table(name = "payment_transaction")
public class Payment extends BaseTenantEntity {

  @Version
  @Column(nullable = false)
  private Long version = 0L;

  @Column(nullable = false)
  private Long orderId;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  })
  private Money amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private PaymentStatus status = PaymentStatus.PENDING;

  /** Gateway name: STRIPE, PAYPAL, CASH, BANK_TRANSFER, etc. */
  @Column(nullable = false, length = 50)
  private String method;

  /** Identifier returned by the payment gateway after processing. */
  @Column(length = 100)
  private String externalId;

  @Column(columnDefinition = "TEXT")
  private String checkoutData;

  @Column(unique = true, length = 100)
  private String idempotencyKey;

  // ── Domain behaviour ───────────────────────────────────────────────────────

  public void markSucceeded(String externalId) {
    this.externalId = externalId;
    this.status = PaymentStatus.SUCCEEDED;
  }

  public void markFailed() {
    this.status = PaymentStatus.FAILED;
  }

  public void markRefunded() {
    if (this.status != PaymentStatus.SUCCEEDED) {
      throw new IllegalStateException("Only succeeded payments can be refunded");
    }
    this.status = PaymentStatus.REFUNDED;
  }

  // ── Bootstrap/JPA setters (Write-once pattern) ──────────────────────────

  public void setOrderId(Long orderId) {
    if (this.orderId != null) throw new IllegalStateException("orderId already set");
    this.orderId = orderId;
  }

  public void setAmount(Money amount) {
    if (this.amount != null) throw new IllegalStateException("amount already set");
    this.amount = amount;
  }

  public void setMethod(String method) {
    if (this.method != null) throw new IllegalStateException("method already set");
    this.method = method;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public void setCheckoutData(String checkoutData) {
    this.checkoutData = checkoutData;
  }

  public void setIdempotencyKey(String idempotencyKey) {
    if (this.idempotencyKey != null) throw new IllegalStateException("idempotencyKey already set");
    this.idempotencyKey = idempotencyKey;
  }
}
