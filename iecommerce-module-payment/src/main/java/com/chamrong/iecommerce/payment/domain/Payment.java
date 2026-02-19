package com.chamrong.iecommerce.payment.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.*;

@Entity
@Table(name = "payment_transaction")
public class Payment extends BaseTenantEntity {

  @Column(nullable = false)
  private Long orderId;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  })
  private Money amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(nullable = false)
  private String method; // e.g., STRIPE, PAYPAL

  private String externalId;

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Money getAmount() {
    return amount;
  }

  public void setAmount(Money amount) {
    this.amount = amount;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }
}
