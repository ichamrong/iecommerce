package com.chamrong.iecommerce.order.domain;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.domain.BaseDomainTenantEntity;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

/**
 * Order Aggregate Root (pure domain — no JPA). Persistence uses {@link
 * com.chamrong.iecommerce.order.infrastructure.persistence.jpa.entity.OrderEntity}.
 *
 * <p>Banking/Insurance: state changes via named methods; OrderStateMachine enforces transitions;
 * constants for limits; unmodifiable getItems(); version for optimistic locking.
 */
@Getter
public class Order extends BaseDomainTenantEntity {

  public static final int MAX_ITEMS = 100;
  public static final int MIN_QUANTITY = 1;
  public static final int MAX_QUANTITY = 9_999;
  private static final String FALLBACK_CURRENCY = "USD";

  private Long version = 0L;
  private String code;
  private Long customerId;
  private OrderState state = OrderState.AddingItems;
  private String shippingAddress;
  private Instant confirmedAt;
  private Instant cancelledAt;
  private Instant shippedAt;
  private String trackingNumber;
  private String voucherCode;
  private Money discount;
  private final List<OrderItem> items = new ArrayList<>();
  private Money subTotal;
  private Money total;
  private Money depositAmount;
  private final List<Object> domainEvents = new ArrayList<>();

  // ── Reconstitution from persistence (used only by OrderPersistenceMapper) ───

  public void setVersion(Long version) {
    this.version = version != null ? version : 0L;
  }

  public void setState(OrderState state) {
    this.state = state != null ? state : OrderState.AddingItems;
  }

  public void setShippingAddress(String shippingAddress) {
    this.shippingAddress = shippingAddress;
  }

  public void setConfirmedAt(Instant confirmedAt) {
    this.confirmedAt = confirmedAt;
  }

  public void setCancelledAt(Instant cancelledAt) {
    this.cancelledAt = cancelledAt;
  }

  public void setShippedAt(Instant shippedAt) {
    this.shippedAt = shippedAt;
  }

  public void setTrackingNumber(String trackingNumber) {
    this.trackingNumber = trackingNumber;
  }

  public void setVoucherCode(String voucherCode) {
    this.voucherCode = voucherCode;
  }

  public void setDiscount(Money discount) {
    this.discount = discount;
  }

  public void setSubTotal(Money subTotal) {
    this.subTotal = subTotal;
  }

  public void setTotal(Money total) {
    this.total = total;
  }

  public void setDepositAmount(Money depositAmount) {
    this.depositAmount = depositAmount;
  }

  /** Reconstitution only: restores items from persistence (sets back-reference on each item). */
  public void restoreItems(List<OrderItem> fromPersistence) {
    items.clear();
    if (fromPersistence != null) {
      for (OrderItem item : fromPersistence) {
        item.assignOrder(this);
        items.add(item);
      }
    }
  }

  // ── Bootstrap (construction / create flow) ──────────────────────────────────

  public void assignCode(final String code) {
    if (this.code != null) {
      throw new IllegalStateException("Order code is already set and cannot be changed");
    }
    this.code = Objects.requireNonNull(code, "Order code must not be null");
  }

  public void assignTenantId(final String tenantId) {
    if (this.getTenantId() != null) {
      throw new IllegalStateException("Tenant ID is already set and cannot be changed");
    }
    Objects.requireNonNull(tenantId, "tenantId must not be null");
    this.setTenantId(tenantId);
  }

  public void updateShippingAddress(final String address) {
    if (this.state == OrderState.Shipped
        || this.state == OrderState.Delivered
        || this.state == OrderState.Completed
        || this.state == OrderState.Cancelled) {
      throw new IllegalStateException(
          "Cannot change shipping address on order in state: " + this.state);
    }
    this.shippingAddress = address;
  }

  public void setCustomerId(final Long customerId) {
    this.customerId = customerId;
  }

  // ── Domain behaviour ───────────────────────────────────────────────────────

  public void addItem(final OrderItem item) {
    Objects.requireNonNull(item, "OrderItem must not be null");
    if (this.items.size() >= MAX_ITEMS) {
      throw new IllegalArgumentException(
          "Order cannot exceed " + MAX_ITEMS + " line items (PCI/fraud limit)");
    }
    final int qty = item.getQuantity() == null ? 0 : item.getQuantity();
    if (qty < MIN_QUANTITY || qty > MAX_QUANTITY) {
      throw new IllegalArgumentException(
          "Quantity "
              + qty
              + " is outside allowed range ["
              + MIN_QUANTITY
              + ", "
              + MAX_QUANTITY
              + "]");
    }
    item.assignOrder(this);
    items.add(item);
  }

  public List<OrderItem> getItems() {
    return Collections.unmodifiableList(items);
  }

  public List<Object> pullDomainEvents() {
    final List<Object> copy = Collections.unmodifiableList(new ArrayList<>(domainEvents));
    domainEvents.clear();
    return copy;
  }

  public void arrangePayment() {
    OrderStateMachine.assertCanTransition(this.state, OrderState.ArrangingPayment);
    if (this.items.isEmpty()) {
      throw new IllegalStateException("Cannot arrange payment for an order with no items");
    }
    this.state = OrderState.ArrangingPayment;
  }

  public void authorizePayment() {
    OrderStateMachine.assertCanTransition(this.state, OrderState.PaymentAuthorized);
    this.state = OrderState.PaymentAuthorized;
  }

  public void confirm() {
    OrderStateMachine.assertCanTransition(this.state, OrderState.Confirmed);
    if (this.state != OrderState.PaymentAuthorized && this.items.isEmpty()) {
      throw new IllegalStateException("Cannot confirm an order with no items");
    }
    this.state = OrderState.Confirmed;
    if (this.confirmedAt == null) {
      this.confirmedAt = Instant.now();
    }
  }

  public void pick() {
    OrderStateMachine.assertCanTransition(this.state, OrderState.Picking);
    this.state = OrderState.Picking;
  }

  public void pack() {
    OrderStateMachine.assertCanTransition(this.state, OrderState.Packing);
    this.state = OrderState.Packing;
  }

  public void ship(final String trackingNumber) {
    OrderStateMachine.assertCanTransition(this.state, OrderState.Shipped);
    if (trackingNumber == null || trackingNumber.isBlank()) {
      throw new IllegalArgumentException("Tracking number is required before shipping");
    }
    this.trackingNumber = trackingNumber.trim();
    this.state = OrderState.Shipped;
    if (this.shippedAt == null) {
      this.shippedAt = Instant.now();
    }
  }

  public void deliver() {
    OrderStateMachine.assertCanTransition(this.state, OrderState.Delivered);
    this.state = OrderState.Delivered;
  }

  public void pay() {
    OrderStateMachine.assertCanTransition(this.state, OrderState.PaymentSettled);
    this.state = OrderState.PaymentSettled;
  }

  public void complete() {
    OrderStateMachine.assertCanTransition(this.state, OrderState.Completed);
    this.state = OrderState.Completed;
  }

  public void completeImmediate() {
    OrderStateMachine.assertCanTransition(this.state, OrderState.Completed);
    this.state = OrderState.Completed;
  }

  public void setTotalManual(Money total) {
    this.total = total;
  }

  public void cancel() {
    OrderStateMachine.assertCanTransition(this.state, OrderState.Cancelled);
    this.state = OrderState.Cancelled;
    if (this.cancelledAt == null) {
      this.cancelledAt = Instant.now();
    }
  }

  public void recalculateTotals() {
    if (this.items.isEmpty()) {
      this.subTotal = null;
      this.total = null;
      return;
    }
    final String currency =
        this.items.stream()
            .filter(i -> i.getUnitPrice() != null)
            .map(i -> i.getUnitPrice().getCurrency())
            .findFirst()
            .orElse(FALLBACK_CURRENCY);
    final BigDecimal sub =
        this.items.stream()
            .filter(i -> i.getUnitPrice() != null)
            .map(i -> computeLineTotal(i))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    this.subTotal = new Money(sub, currency);
    BigDecimal grandTotal = sub;
    if (this.discount != null && this.discount.getAmount() != null) {
      grandTotal = grandTotal.subtract(this.discount.getAmount());
    }
    this.total = new Money(grandTotal.max(BigDecimal.ZERO), currency);
  }

  public void applyVoucher(final Money discount, final String code) {
    Objects.requireNonNull(discount, "Discount must not be null");
    Objects.requireNonNull(code, "Voucher code must not be null");
    if (code.isBlank()) {
      throw new IllegalArgumentException("Voucher code must not be blank");
    }
    if (this.state != OrderState.AddingItems) {
      throw new IllegalStateException("Voucher can only be applied to orders in AddingItems state");
    }
    if (this.subTotal == null || this.subTotal.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalStateException("Cannot apply voucher to an order with no items");
    }
    this.voucherCode = code;
    this.discount = discount;
    recalculateTotals();
  }

  private static BigDecimal computeLineTotal(final OrderItem item) {
    BigDecimal unitPrice = item.getUnitPrice().getAmount();
    BigDecimal lineAmount = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
    if (item.getStartAt() != null && item.getEndAt() != null) {
      final long days = Duration.between(item.getStartAt(), item.getEndAt()).toDays();
      if (days > 0) {
        lineAmount = lineAmount.multiply(BigDecimal.valueOf(days));
      }
    }
    return lineAmount;
  }
}
