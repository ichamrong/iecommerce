package com.chamrong.iecommerce.order.application.dto;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
    Long id,
    String code,
    Long customerId,
    String state,
    List<OrderItemResponse> items,
    Money subTotal,
    Money total,
    String shippingAddress,
    String trackingNumber,
    String voucherCode,
    Money discount,
    Instant createdAt,
    Instant updatedAt) {

  public record OrderItemResponse(
      Long id,
      Long productVariantId,
      Integer quantity,
      Money unitPrice,
      Instant startAt,
      Instant endAt) {}
}
