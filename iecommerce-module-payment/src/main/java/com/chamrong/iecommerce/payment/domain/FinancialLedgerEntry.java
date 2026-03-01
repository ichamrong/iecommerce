package com.chamrong.iecommerce.payment.domain;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Entity: Immutable record of money movement. */
public class FinancialLedgerEntry {

  private final UUID entryId;
  private final String tenantId;
  private final Long orderId;
  private final UUID paymentIntentId;
  private final Money amount;
  private final LedgerType type;
  private final String description;
  private final Instant createdAt;

  public FinancialLedgerEntry(
      UUID entryId,
      String tenantId,
      Long orderId,
      UUID paymentIntentId,
      Money amount,
      LedgerType type,
      String description) {
    this.entryId = Objects.requireNonNull(entryId);
    this.tenantId = Objects.requireNonNull(tenantId);
    this.orderId = Objects.requireNonNull(orderId);
    this.paymentIntentId = Objects.requireNonNull(paymentIntentId);
    this.amount = Objects.requireNonNull(amount);
    this.type = Objects.requireNonNull(type);
    this.description = description;
    this.createdAt = Instant.now();
  }

  public enum LedgerType {
    CREDIT, // Money IN (e.g. from Guest)
    DEBIT // Money OUT (e.g. to Owner or Refund)
  }

  public UUID getEntryId() {
    return entryId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public UUID getPaymentIntentId() {
    return paymentIntentId;
  }

  public Money getAmount() {
    return amount;
  }

  public LedgerType getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
