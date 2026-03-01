package com.chamrong.iecommerce.sale.domain.model;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.sale.domain.exception.SaleDomainException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Quotation aggregate root. PURE Domain Model: No JPA/Spring annotations. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class Quotation {

  private Long id;
  private String tenantId;
  private Long version;
  private String customerId;
  private Instant expiryDate;
  private Money totalAmount;
  private QuotationStatus status;
  private List<QuotationItem> items = new ArrayList<>();
  private Instant createdAt;
  private Instant updatedAt;

  public enum QuotationStatus {
    DRAFT,
    SENT,
    CONFIRMED,
    REJECTED,
    EXPIRED,
    CANCELED
  }

  public Quotation(String tenantId, String customerId, String currency, Instant expiryDate) {
    this.tenantId = tenantId;
    this.customerId = customerId;
    this.expiryDate = expiryDate;
    this.status = QuotationStatus.DRAFT;
    this.totalAmount = Money.zero(currency);
  }

  // Used by mapper/factory
  public Quotation(
      Long id,
      String tenantId,
      Long version,
      String customerId,
      Instant expiryDate,
      QuotationStatus status,
      Money totalAmount,
      List<QuotationItem> items,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.version = version;
    this.customerId = customerId;
    this.expiryDate = expiryDate;
    this.status = status;
    this.totalAmount = totalAmount;
    this.items = new ArrayList<>(items);
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public void addItem(String productId, java.math.BigDecimal quantity, Money unitPrice) {
    checkNotConfirmed();
    QuotationItem item = new QuotationItem(this, productId, quantity, unitPrice);
    this.items.add(item);
    recalculateTotals();
  }

  public void removeItem(Long itemId) {
    checkNotConfirmed();
    items.removeIf(item -> item.getId() != null && item.getId().equals(itemId));
    recalculateTotals();
  }

  public void confirm() {
    if (this.status != QuotationStatus.DRAFT && this.status != QuotationStatus.SENT) {
      throw new SaleDomainException("Only DRAFT or SENT quotations can be confirmed");
    }
    if (items.isEmpty()) {
      throw new SaleDomainException("Cannot confirm an empty quotation");
    }
    this.status = QuotationStatus.CONFIRMED;
    log.info("Quotation {} confirmed for tenant {}", this.id, this.tenantId);
  }

  public void cancel() {
    if (this.status == QuotationStatus.CONFIRMED) {
      throw new SaleDomainException("Cannot cancel a confirmed quotation");
    }
    this.status = QuotationStatus.CANCELED;
  }

  public void expire() {
    if (this.status == QuotationStatus.CONFIRMED) {
      return;
    }
    this.status = QuotationStatus.EXPIRED;
  }

  private void recalculateTotals() {
    if (items.isEmpty()) {
      this.totalAmount = Money.zero(totalAmount.getCurrency());
      return;
    }
    java.math.BigDecimal total =
        items.stream()
            .map(i -> i.getTotalPrice().getAmount())
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

    this.totalAmount = new Money(total, totalAmount.getCurrency());
  }

  private void checkNotConfirmed() {
    if (this.status == QuotationStatus.CONFIRMED) {
      throw new SaleDomainException("Cannot modify a confirmed quotation");
    }
  }

  public List<QuotationItem> getItems() {
    return Collections.unmodifiableList(items);
  }
}
