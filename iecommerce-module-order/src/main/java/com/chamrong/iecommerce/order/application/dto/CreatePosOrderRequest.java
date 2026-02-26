package com.chamrong.iecommerce.order.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreatePosOrderRequest(
    Long customerId,
    List<Item> items,
    String paymentMethod,
    BigDecimal amountPaid,
    String currency) {

  public record Item(Long productVariantId, int quantity, BigDecimal price) {}
}
