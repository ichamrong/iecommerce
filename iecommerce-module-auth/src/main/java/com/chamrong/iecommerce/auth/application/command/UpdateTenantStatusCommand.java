package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.domain.TenantStatus;

public record UpdateTenantStatusCommand(String tenantId, TenantStatus status) {}
