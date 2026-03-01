package com.chamrong.iecommerce.sale.application.command;

import jakarta.validation.constraints.NotBlank;

public record OpenShiftCommand(
    @NotBlank String tenantId, @NotBlank String staffId, @NotBlank String terminalId) {}
