package com.chamrong.iecommerce.payment.domain.ledger;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: an immutable record of a single money movement in the financial ledger.
 *
 * <p>Uses double-entry bookkeeping: every transaction has a matching CREDIT and DEBIT entry.
 *
 * <p>This class is pure Java with no Spring or JPA dependencies.
 */
public final class FinancialLedgerEntry {

  private final UUID entryId;
  private final String tenantId;
  private final Long orderId;
  private final UUID paymentIntentId;
  private final Money amount;
  private final EntryType type;
  private final String description;
  private final Instant createdAt;

  public FinancialLedgerEntry(
      UUID entryId,
      String tenantId,
      Long orderId,
      UUID paymentIntentId,
      Money amount,
      EntryType type,
      String description) {
    this.entryId = Objects.requireNonNull(entryId, "entryId required");
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId required");
    this.orderId = Objects.requireNonNull(orderId, "orderId required");
    this.paymentIntentId = Objects.requireNonNull(paymentIntentId, "paymentIntentId required");
    this.amount = Objects.requireNonNull(amount, "amount required");
    this.type = Objects.requireNonNull(type, "type required");
    this.description = description;
    this.createdAt = Instant.now();
  }

  // ── Factory methods ────────────────────────────────────────────────────────

  public static FinancialLedgerEntry credit(
      String tenantId, Long orderId, UUID paymentIntentId, Money amount, String description) {
    return new FinancialLedgerEntry(
        UUID.randomUUID(),
        tenantId,
        orderId,
        paymentIntentId,
        amount,
        EntryType.CREDIT,
        description);
  }

  public static FinancialLedgerEntry debit(
      String tenantId, Long orderId, UUID paymentIntentId, Money amount, String description) {
    return new FinancialLedgerEntry(
        UUID.randomUUID(),
        tenantId,
        orderId,
        paymentIntentId,
        amount,
        EntryType.DEBIT,
        description);
  }

  // ── Getters ────────────────────────────────────────────────────────────────

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

  public EntryType getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  // ── Nested types ───────────────────────────────────────────────────────────

  public enum EntryType {
    CREDIT, // Money in (e.g. from guest payment)
    DEBIT // Money out (e.g. refund to customer, payout to merchant)
  }
}
