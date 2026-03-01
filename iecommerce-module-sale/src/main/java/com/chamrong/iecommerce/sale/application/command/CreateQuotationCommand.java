package com.chamrong.iecommerce.sale.application.command;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CreateQuotationCommand(
    @NotBlank String tenantId,
    @NotBlank String customerId,
    @NotBlank String currency,
    @Future @NotNull Instant expiryDate,
    @NotEmpty List<QuotationItemLine> items) {
  public record QuotationItemLine(
      @NotBlank String productId, @NotNull BigDecimal quantity, @NotNull BigDecimal unitPrice) {}
}
