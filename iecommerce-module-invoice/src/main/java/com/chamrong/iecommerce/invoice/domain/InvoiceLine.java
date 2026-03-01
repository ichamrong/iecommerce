package com.chamrong.iecommerce.invoice.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * A single line item on an {@link Invoice}.
 *
 * <p>Subtotal is always computed deterministically as {@code quantity × unitPrice} using {@link
 * RoundingMode#HALF_EVEN}.
 *
 * <p>No setters are exposed after construction; modifications are made via the parent aggregate.
 */
@Entity
@Table(name = "invoice_line")
public class InvoiceLine extends BaseEntity {

  /** Required by JPA — not for application use. */
  protected InvoiceLine() {}

  @Column(length = 100)
  private String sku;

  @Column(nullable = false, length = 255)
  private String productName;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private int quantity;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price", nullable = false)),
    @AttributeOverride(
        name = "currency",
        column = @Column(name = "currency", nullable = false, length = 3))
  })
  private Money unitPrice;

  /**
   * Tax rate expressed as a decimal fraction (e.g., {@code 0.10} for 10%). Stored to 4 decimal
   * places. May be zero.
   */
  @Column(name = "tax_rate", nullable = false, precision = 7, scale = 4)
  private BigDecimal taxRate = BigDecimal.ZERO;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "subtotal", nullable = false)),
    @AttributeOverride(
        name = "currency",
        column = @Column(name = "subtotal_currency", nullable = false, length = 3))
  })
  private Money subtotal;

  /** Sort order within the invoice for deterministic rendering. */
  @Column(name = "line_order", nullable = false)
  private int lineOrder;

  /**
   * Factory: creates a validated line item and computes its subtotal.
   *
   * @param sku optional SKU / product code
   * @param productName display name — must not be blank
   * @param description optional extended description
   * @param quantity must be &ge; 1
   * @param unitPrice must be non-null and non-negative
   * @param taxRate tax rate fraction — must be &ge; 0
   * @param lineOrder 0-based sort order
   */
  public static InvoiceLine of(
      String sku,
      String productName,
      String description,
      int quantity,
      Money unitPrice,
      BigDecimal taxRate,
      int lineOrder) {
    Objects.requireNonNull(productName, "productName must not be null");
    Objects.requireNonNull(unitPrice, "unitPrice must not be null");
    Objects.requireNonNull(taxRate, "taxRate must not be null");
    if (productName.isBlank()) throw new IllegalArgumentException("productName must not be blank");
    if (quantity < 1) throw new IllegalArgumentException("quantity must be >= 1");
    if (taxRate.compareTo(BigDecimal.ZERO) < 0)
      throw new IllegalArgumentException("taxRate must be >= 0");

    InvoiceLine line = new InvoiceLine();
    line.sku = sku;
    line.productName = productName;
    line.description = description;
    line.quantity = quantity;
    line.unitPrice = unitPrice;
    line.taxRate = taxRate.setScale(4, RoundingMode.HALF_EVEN);
    line.lineOrder = lineOrder;
    line.subtotal = line.computeSubtotal();
    return line;
  }

  /**
   * Computes {@code quantity × unitPrice} using {@link RoundingMode#HALF_EVEN}.
   *
   * @return the line subtotal (before tax)
   */
  public Money computeSubtotal() {
    return unitPrice.multiply(quantity);
  }

  /**
   * Computes the tax amount for this line: {@code subtotal × taxRate}.
   *
   * @return tax portion of this line
   */
  public Money computeTaxAmount() {
    return subtotal.multiply(taxRate);
  }

  // ── Accessors (read-only for domain callers) ──────────────────────────────

  public String getSku() {
    return sku;
  }

  public String getProductName() {
    return productName;
  }

  public String getDescription() {
    return description;
  }

  public int getQuantity() {
    return quantity;
  }

  public Money getUnitPrice() {
    return unitPrice;
  }

  public BigDecimal getTaxRate() {
    return taxRate;
  }

  public Money getSubtotal() {
    return subtotal;
  }

  public int getLineOrder() {
    return lineOrder;
  }
}
