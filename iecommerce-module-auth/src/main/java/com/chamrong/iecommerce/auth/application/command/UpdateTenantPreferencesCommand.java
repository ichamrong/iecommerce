package com.chamrong.iecommerce.auth.application.command;

import org.springframework.lang.Nullable;

public record UpdateTenantPreferencesCommand(
    String tenantId,
    @Nullable String logoUrl,
    @Nullable String primaryColor,
    @Nullable String secondaryColor,
    @Nullable String fontFamily) {}
