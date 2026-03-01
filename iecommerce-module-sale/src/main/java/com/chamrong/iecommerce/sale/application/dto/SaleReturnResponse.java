package com.chamrong.iecommerce.sale.application.dto;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;
import java.util.List;

public record SaleReturnResponse(
    Long id,
    Long originalOrderId,
    String returnKey,
    String status,
    String reason,
    Money totalRefundAmount,
    Instant requestedAt,
    Instant completedAt,
    List<ReturnItemResponse> items) {
  public record ReturnItemResponse(
      Long id,
      Long originalLineId,
      java.math.BigDecimal quantity,
      Money refundPrice,
      Money totalRefundAmount) {}
}
