package com.chamrong.iecommerce.payment.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.*;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "payment_transaction")
public class Payment extends BaseTenantEntity {

  public Payment() {}

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

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

  public Long getVersion() {
    return version;
  }

  public String getTenantId() {
    return super.getTenantId();
  }

  public Long getOrderId() {
    return orderId;
  }

  public Money getAmount() {
    return amount;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public String getMethod() {
    return method;
  }

  public String getExternalId() {
    return externalId;
  }

  public String getCheckoutData() {
    return checkoutData;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  // ── Domain behaviour ───────────────────────────────────────────────────────

  public void markSucceeded(String externalId) {
    this.externalId = externalId;
    this.status = PaymentStateMachine.onAuthorizedOrCaptured(this.status);
  }

  public void markFailed() {
    this.status = PaymentStateMachine.onFailure(this.status);
  }

  public void markRefunded() {
    this.status = PaymentStateMachine.onRefund(this.status);
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    Payment payment = (Payment) o;
    return getId() != null && Objects.equals(getId(), payment.getId());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
