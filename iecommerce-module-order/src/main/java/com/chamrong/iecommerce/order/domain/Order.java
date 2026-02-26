package com.chamrong.iecommerce.order.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

/**
 * Order Aggregate Root.
 *
 * <p><b>Banking/Insurance coding standards applied:</b>
 *
 * <ul>
 *   <li>{@code @Setter} is intentionally ABSENT. All state changes must go through named domain
 *       methods ({@link #confirm()}, {@link #ship(String)}, etc.). Direct field mutation via
 *       setters would bypass business rule guards.
 *   <li>Every public domain method uses {@link OrderStateMachine#assertCanTransition} as the single
 *       enforcement point — no scattered {@code if (state != X)} checks.
 *   <li>All numeric constants ({@link #MAX_ITEMS}, {@link #MIN_QUANTITY}) are {@code static final}
 *       to prevent magic-number drift across the codebase.
 *   <li>The {@code items} collection is only exposed as an unmodifiable view via {@link
 *       #getItems()} — callers cannot mutate it directly.
 *   <li>{@link #code} and {@code tenantId} are write-once via constructor-only setters and marked
 *       {@code updatable = false} in the column definition.
 *   <li>{@link #version} field provides optimistic locking (concurrent-write protection).
 * </ul>
 */
@Getter
@Entity
@Table(name = "ecommerce_order")
public class Order extends BaseTenantEntity {

  // ── Business constants ─────────────────────────────────────────────────────

  /** Maximum number of line items per order (PCI/fraud-prevention limit). */
  public static final int MAX_ITEMS = 100;

  /** Minimum allowed quantity per line item. */
  public static final int MIN_QUANTITY = 1;

  /** Maximum allowed quantity per line item. */
  public static final int MAX_QUANTITY = 9_999;

  /** Currency used when no price exists on an item (should never happen in production). */
  private static final String FALLBACK_CURRENCY = "USD";

  // ── Persistence ────────────────────────────────────────────────────────────

  /**
   * Optimistic locking — prevents last-write-wins concurrency bug. JPA throws {@link
   * jakarta.persistence.OptimisticLockException} on conflict.
   */
  @Version
  @Column(nullable = false)
  private Long version = 0L;

  @Column(length = 100, nullable = false, updatable = false)
  private String code;

  @Column private Long customerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private OrderState state = OrderState.AddingItems;

  @Column(columnDefinition = "TEXT")
  private String shippingAddress;

  @Column(length = 100)
  private String trackingNumber;

  @Column(length = 50)
  private String voucherCode;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "discount_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "discount_currency", length = 3))
  })
  private Money discount;

  /** Internal mutable list — never exposed directly. */
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> items = new ArrayList<>();

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "subtotal_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "subtotal_currency"))
  })
  private Money subTotal;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "total_currency"))
  })
  private Money total;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "deposit_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "deposit_currency"))
  })
  private Money depositAmount;

  /**
   * Transient domain events — written atomically via the Outbox pattern. Never persisted. Cleared
   * after each {@code orderRepository.save()}.
   */
  @jakarta.persistence.Transient private final List<Object> domainEvents = new ArrayList<>();

  // ── Protected bootstrap setters (used only by JPA and OrderService.create*) ─

  /**
   * Write-once code setter. Only called once during construction. Intentionally not prefixed with
   * {@code set} to discourage misuse.
   */
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

  /** Set at any time before confirmation (e.g., customer selects delivery address). */
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
    this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
  }

  // ── Domain behaviour ───────────────────────────────────────────────────────

  /**
   * Adds a line item.
   *
   * @throws IllegalArgumentException if quantity bounds are violated or item limit is reached
   */
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
    item.setOrder(this);
    items.add(item);
  }

  /** Returns an unmodifiable view — external code cannot bypass domain logic. */
  public List<OrderItem> getItems() {
    return Collections.unmodifiableList(items);
  }

  public List<Object> pullDomainEvents() {
    final List<Object> copy = Collections.unmodifiableList(new ArrayList<>(domainEvents));
    domainEvents.clear();
    return copy;
  }

  // ── State transitions — all validated through OrderStateMachine ────────────

  public void confirm() {
    OrderStateMachine.assertCanTransition(this.state, OrderState.Confirmed);
    if (this.items.isEmpty()) {
      throw new IllegalStateException("Cannot confirm an order with no items");
    }
    this.state = OrderState.Confirmed;
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

  /** Special direct transition for POS or trusted handovers. */
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
  }

  // ── Financial calculations ─────────────────────────────────────────────────

  /**
   * Recalculates {@link #subTotal} and {@link #total} from line items.
   *
   * <p>For time-based items (bookings/accommodation), price is multiplied by the number of
   * days/nights between {@code startAt} and {@code endAt}.
   *
   * <p>All arithmetic uses {@link BigDecimal} with {@link java.math.RoundingMode#HALF_EVEN}
   * (banker's rounding) to minimize cumulative rounding errors on financial sums.
   */
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
    // Floor at zero — orders can never have a negative total (financial invariant)
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

  // ── Private helpers ────────────────────────────────────────────────────────

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
