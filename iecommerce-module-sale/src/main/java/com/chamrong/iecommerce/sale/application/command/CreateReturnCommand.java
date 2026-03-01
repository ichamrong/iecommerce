package com.chamrong.iecommerce.sale.application.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public record CreateReturnCommand(
    @NotBlank String tenantId,
    @NotNull Long originalOrderId,
    @NotBlank String returnKey,
    @NotBlank String reason,
    @NotBlank String currency,
    @NotEmpty List<@Valid ReturnItemLine> items) {
  public record ReturnItemLine(
      @NotNull Long originalLineId,
      @NotNull @Positive BigDecimal quantity,
      @NotNull @PositiveOrZero BigDecimal refundPrice) {}
}
