package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.domain.TenantPlan;
import org.springframework.lang.Nullable;

/** Command to update tenant profile (name, plan). Used by admin PUT /api/v1/admin/tenants/:id. */
public record UpdateTenantCommand(
    String tenantCode,
    @Nullable String name,
    @Nullable TenantPlan plan,
    @Nullable java.time.Instant trialEndsAt) {}
