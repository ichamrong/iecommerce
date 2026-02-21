package com.chamrong.iecommerce.auth.application.command;

public record UpdateTenantPreferencesCommand(
    String tenantId,
    String logoUrl,
    String primaryColor,
    String secondaryColor,
    String fontFamily) {}
