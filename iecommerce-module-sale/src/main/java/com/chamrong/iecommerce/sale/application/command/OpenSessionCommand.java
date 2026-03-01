package com.chamrong.iecommerce.sale.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OpenSessionCommand(
    @NotBlank String tenantId,
    @NotBlank String terminalId,
    @NotBlank String currency,
    @NotNull Long shiftId) {}
