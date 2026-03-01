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

/** SaleReturn aggregate. PURE Domain Model: No JPA annotations. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class SaleReturn {

  private Long id;
  private String tenantId;
  private Long version;
  private Long originalOrderId;
  private String returnKey;
  private ReturnStatus status;
  private String reason;
  private Money totalRefundAmount;
  private List<ReturnItem> items = new ArrayList<>();
  private Instant requestedAt;
  private Instant completedAt;

  /** Set by persistence layer for keyset pagination (created_at DESC, id DESC). */
  private Instant createdAt;

  public enum ReturnStatus {
    REQUESTED,
    APPROVED,
    REJECTED,
    COMPLETED
  }

  public SaleReturn(
      String tenantId, Long originalOrderId, String returnKey, String reason, String currency) {
    this.tenantId = tenantId;
    this.originalOrderId = originalOrderId;
    this.returnKey = returnKey;
    this.reason = reason;
    this.status = ReturnStatus.REQUESTED;
    this.totalRefundAmount = Money.zero(currency);
    this.requestedAt = Instant.now();
  }

  // Factory constructor for mapper
  public SaleReturn(
      Long id,
      String tenantId,
      Long version,
      Long originalOrderId,
      String returnKey,
      ReturnStatus status,
      String reason,
      Money totalRefundAmount,
      List<ReturnItem> items,
      Instant requestedAt,
      Instant completedAt,
      Instant createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.version = version;
    this.originalOrderId = originalOrderId;
    this.returnKey = returnKey;
    this.status = status;
    this.reason = reason;
    this.totalRefundAmount = totalRefundAmount;
    this.items = new ArrayList<>(items);
    this.requestedAt = requestedAt;
    this.completedAt = completedAt;
    this.createdAt = createdAt;
  }

  public void addItem(Long originalLineId, java.math.BigDecimal quantity, Money refundPrice) {
    if (this.status != ReturnStatus.REQUESTED) {
      throw new SaleDomainException("Can only add items to REQUESTED returns");
    }
    ReturnItem item = new ReturnItem(this, originalLineId, quantity, refundPrice);
    this.items.add(item);
    recalculateTotals();
  }

  public void approve(String approverId) {
    if (this.status != ReturnStatus.REQUESTED) {
      throw new SaleDomainException("Only REQUESTED returns can be approved");
    }
    this.status = ReturnStatus.APPROVED;
    log.info("Return {} approved by {}", this.id, approverId);
  }

  public void reject(String reason) {
    if (this.status != ReturnStatus.REQUESTED) {
      throw new SaleDomainException("Only REQUESTED returns can be rejected");
    }
    this.status = ReturnStatus.REJECTED;
    log.info("Return {} rejected: {}", this.id, reason);
  }

  public void complete() {
    if (this.status != ReturnStatus.APPROVED) {
      throw new SaleDomainException("Only APPROVED returns can be completed");
    }
    this.status = ReturnStatus.COMPLETED;
    this.completedAt = Instant.now();
  }

  private void recalculateTotals() {
    java.math.BigDecimal total =
        items.stream()
            .map(i -> i.getTotalRefundAmount().getAmount())
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    this.totalRefundAmount = new Money(total, totalRefundAmount.getCurrency());
  }

  public List<ReturnItem> getItems() {
    return Collections.unmodifiableList(items);
  }

  public Money getTotalAmount() {
    return totalRefundAmount;
  }
}
