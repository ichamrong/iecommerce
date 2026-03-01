package com.chamrong.iecommerce.sale.domain.model;

import com.chamrong.iecommerce.common.Money;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** ReturnItem domain model. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReturnItem {

  private Long id;
  private SaleReturn saleReturn;
  private Long originalLineId;
  private BigDecimal quantity;
  private Money refundPrice;
  private Money totalRefundAmount;

  ReturnItem(SaleReturn saleReturn, Long originalLineId, BigDecimal quantity, Money refundPrice) {
    this.saleReturn = saleReturn;
    this.originalLineId = originalLineId;
    this.quantity = quantity;
    this.refundPrice = refundPrice;
    this.totalRefundAmount = refundPrice.multiply(quantity);
  }

  // Factory constructor for mapper
  public ReturnItem(
      Long id,
      SaleReturn saleReturn,
      Long originalLineId,
      BigDecimal quantity,
      Money refundPrice,
      Money totalRefundAmount) {
    this.id = id;
    this.saleReturn = saleReturn;
    this.originalLineId = originalLineId;
    this.quantity = quantity;
    this.refundPrice = refundPrice;
    this.totalRefundAmount = totalRefundAmount;
  }
}
