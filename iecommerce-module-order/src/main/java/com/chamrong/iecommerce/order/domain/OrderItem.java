package com.chamrong.iecommerce.order.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ecommerce_order_item")
public class OrderItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Order order;

  /** Cross-boundary reference to catalog's ProductVariant. */
  @Column(name = "product_variant_id")
  private Long productVariantId;

  @Column(nullable = false)
  private Integer quantity;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
  })
  private Money unitPrice;

  /** For time-based items (bookings, accommodations). */
  private java.time.Instant startAt;

  private java.time.Instant endAt;

  public OrderItem() {}

  /**
   * Called only by {@link Order#addItem(OrderItem)} — package-private to prevent external misuse.
   */
  void assignOrder(Order order) {
    this.order = order;
  }

  // ── Factory ───────────────────────────────────────────────────────────────

  public static OrderItem of(
      Long productVariantId,
      Integer quantity,
      Money unitPrice,
      java.time.Instant startAt,
      java.time.Instant endAt) {
    var item = new OrderItem();
    item.productVariantId = productVariantId;
    item.quantity = quantity;
    item.unitPrice = unitPrice;
    item.startAt = startAt;
    item.endAt = endAt;
    return item;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public Long getProductVariantId() {
    return productVariantId;
  }

  public void setProductVariantId(Long productVariantId) {
    this.productVariantId = productVariantId;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public Money getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(Money unitPrice) {
    this.unitPrice = unitPrice;
  }

  public java.time.Instant getStartAt() {
    return startAt;
  }

  public void setStartAt(java.time.Instant startAt) {
    this.startAt = startAt;
  }

  public java.time.Instant getEndAt() {
    return endAt;
  }

  public void setEndAt(java.time.Instant endAt) {
    this.endAt = endAt;
  }

  // ── Behaviour ────────────────────────────────────────────────────────────

  /** Merge quantity on an existing line (used by add-item use case). */
  public void updateQuantity(int newQuantity) {
    this.quantity = newQuantity;
  }
}
