package com.chamrong.iecommerce.payment.domain;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;
import java.util.Objects;

/** Entity: Represents a specific event or record from a payment provider. */
public class PaymentTransaction {

  private final Long id; // DB generated
  private final String providerEventId;
  private final TransactionType type;
  private final Money amount;
  private final String rawStatus;
  private final Instant createdAt;

  public PaymentTransaction(
      Long id, String providerEventId, TransactionType type, Money amount, String rawStatus) {
    this.id = id;
    this.providerEventId = providerEventId;
    this.type = Objects.requireNonNull(type);
    this.amount = Objects.requireNonNull(amount);
    this.rawStatus = rawStatus;
    this.createdAt = Instant.now();
  }

  public enum TransactionType {
    AUTHORIZE,
    CAPTURE,
    SALE,
    REFUND,
    REVERSAL
  }

  public Long getId() {
    return id;
  }

  public String getProviderEventId() {
    return providerEventId;
  }

  public TransactionType getType() {
    return type;
  }

  public Money getAmount() {
    return amount;
  }

  public String getRawStatus() {
    return rawStatus;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
