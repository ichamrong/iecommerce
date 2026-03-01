package com.chamrong.iecommerce.payment.domain;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root: Represents the business intent to collect a specific amount of money. This class
 * is pure Java and contains all business invariants and state machine rules. Aligned with
 * bank-grade security for idempotent payment processing.
 */
public class PaymentIntent {

  private final UUID intentId;
  private final String tenantId;
  private final Long orderId;
  private final Money amount;
  private final ProviderType provider;
  private final String idempotencyKey;

  private PaymentStatus status;
  private String externalId; // Provider reference (e.g. pi_123 or PayPal Order ID)
  private String checkoutUrl;
  private String clientSecret;
  private String failureCode;
  private String failureMessage;
  private String qrCode;
  private String deepLink;

  private final Instant createdAt;
  private Instant updatedAt;
  private long version;

  private final List<PaymentTransaction> transactions = new ArrayList<>();

  /** Creating a new intent. */
  public PaymentIntent(
      UUID intentId,
      String tenantId,
      Long orderId,
      Money amount,
      ProviderType provider,
      String idempotencyKey) {
    this.intentId = Objects.requireNonNull(intentId);
    this.tenantId = Objects.requireNonNull(tenantId);
    this.orderId = Objects.requireNonNull(orderId);
    this.amount = Objects.requireNonNull(amount);
    if (amount.getAmount().signum() <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    this.provider = Objects.requireNonNull(provider);
    this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
    this.status = PaymentStatus.CREATED;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  // ── Business Logic ────────────────────────────────────────────────────────

  public void start(
      String externalId, String checkoutUrl, String clientSecret, String qrCode, String deepLink) {
    if (this.status != PaymentStatus.CREATED) {
      throw new IllegalStateException("Cannot start payment in status: " + status);
    }
    this.externalId = externalId;
    this.checkoutUrl = checkoutUrl;
    this.clientSecret = clientSecret;
    this.qrCode = qrCode;
    this.deepLink = deepLink;
    this.status = PaymentStatus.REQUIRES_ACTION;
    this.updatedAt = Instant.now();
  }

  public void succeed(String externalId) {
    if (this.status == PaymentStatus.SUCCEEDED) return;
    if (this.status.isTerminal()) {
      throw new PaymentException("Cannot transition to SUCCEEDED from terminal state: " + status);
    }
    this.status = PaymentStatus.SUCCEEDED;
    this.externalId = externalId;
    this.updatedAt = Instant.now();
  }

  public void fail(String code, String message) {
    if (this.status == PaymentStatus.SUCCEEDED) {
      throw new PaymentException("Cannot fail a succeeded payment");
    }
    if (this.status.isTerminal()) return;

    this.status = PaymentStatus.FAILED;
    this.failureCode = code;
    this.failureMessage = message;
    this.updatedAt = Instant.now();
  }

  public void recordTransaction(PaymentTransaction tx) {
    Objects.requireNonNull(tx, "Transaction cannot be null");
    this.transactions.add(tx);
    this.updatedAt = Instant.now();

    // Auto-update status based on transaction type
    if (tx.getType() == PaymentTransaction.TransactionType.CAPTURE
        || tx.getType() == PaymentTransaction.TransactionType.SALE) {
      if (tx.getAmount().equals(this.amount)) {
        this.succeed(tx.getProviderEventId());
      }
    }
  }

  // ── Getters ───────────────────────────────────────────────────────────────

  public UUID getIntentId() {
    return intentId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public Money getAmount() {
    return amount;
  }

  public ProviderType getProvider() {
    return provider;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public String getExternalId() {
    return externalId;
  }

  public String getCheckoutUrl() {
    return checkoutUrl;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getFailureCode() {
    return failureCode;
  }

  public String getFailureMessage() {
    return failureMessage;
  }

  public String getQrCode() {
    return qrCode;
  }

  public String getDeepLink() {
    return deepLink;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public long getVersion() {
    return version;
  }

  public List<PaymentTransaction> getTransactions() {
    return Collections.unmodifiableList(transactions);
  }

  // ── Persistence Helpers (Package Private) ───────────────────────────────

  public void setVersion(long version) {
    this.version = version;
  }
}
