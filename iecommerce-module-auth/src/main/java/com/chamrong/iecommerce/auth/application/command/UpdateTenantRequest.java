package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.domain.TenantPlan;
import java.time.Instant;
import org.springframework.lang.Nullable;

/** Request body for PUT /api/v1/admin/tenants/:id. */
public record UpdateTenantRequest(
    @Nullable String name, @Nullable TenantPlan plan, @Nullable Instant trialEndsAt) {}
