package com.chamrong.iecommerce.invoice.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.*;

@Entity
@Table(name = "invoice_line")
public class InvoiceLine extends BaseEntity {

  @Column(nullable = false)
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
}
