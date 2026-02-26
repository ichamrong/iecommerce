package com.chamrong.iecommerce.sale.application.dto;

import com.chamrong.iecommerce.common.Money;
import java.util.List;

public record ReturnResponse(
    Long id,
    String orderId,
    String reason,
    Money refundAmount,
    String status,
    List<ReturnItemResponse> items) {

  public record ReturnItemResponse(
      Long id, String productId, java.math.BigDecimal quantity, String condition) {}
}
