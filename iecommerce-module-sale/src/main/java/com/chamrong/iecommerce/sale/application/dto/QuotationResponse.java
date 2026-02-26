package com.chamrong.iecommerce.sale.application.dto;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;
import java.util.List;

public record QuotationResponse(
    Long id,
    String customerId,
    Instant expiryDate,
    Money totalAmount,
    String status,
    List<QuotationItemResponse> items) {

  public record QuotationItemResponse(
      Long id,
      String productId,
      java.math.BigDecimal quantity,
      Money unitPrice,
      Money totalPrice) {}
}
