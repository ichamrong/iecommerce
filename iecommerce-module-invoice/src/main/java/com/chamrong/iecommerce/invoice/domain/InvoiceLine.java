package com.chamrong.iecommerce.invoice.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoice_line")
public class InvoiceLine extends BaseEntity {

  public InvoiceLine() {}

  @Column(nullable = false, length = 255)
  private String productName;

  @Column(nullable = false)
  private Integer quantity;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price")),
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  })
  private Money unitPrice;

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
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

  public InvoiceLine(String productName, Integer quantity, Money unitPrice) {
    this.productName = productName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
  }
}
