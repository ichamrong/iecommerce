package com.chamrong.iecommerce.sale.domain.model;

import com.chamrong.iecommerce.common.Money;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** QuotationItem domain model. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuotationItem {

  private Long id;
  private Quotation quotation;
  private String productId;
  private BigDecimal quantity;
  private Money unitPrice;
  private Money totalPrice;

  QuotationItem(Quotation quotation, String productId, BigDecimal quantity, Money unitPrice) {
    this.quotation = quotation;
    this.productId = productId;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.totalPrice = unitPrice.multiply(quantity);
  }

  // Factory constructor for mapper
  public QuotationItem(
      Long id,
      Quotation quotation,
      String productId,
      BigDecimal quantity,
      Money unitPrice,
      Money totalPrice) {
    this.id = id;
    this.quotation = quotation;
    this.productId = productId;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.totalPrice = totalPrice;
  }

  void updateQuantity(BigDecimal newQuantity) {
    this.quantity = newQuantity;
    this.totalPrice = unitPrice.multiply(newQuantity);
  }
}
