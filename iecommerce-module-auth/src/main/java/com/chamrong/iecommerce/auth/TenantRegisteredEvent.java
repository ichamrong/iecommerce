package com.chamrong.iecommerce.auth;

import com.chamrong.iecommerce.auth.domain.TenantPlan;
import com.chamrong.iecommerce.auth.domain.TenantStatus;

/** Event published when a new tenant signs up. */
public record TenantRegisteredEvent(
    String tenantCode, String name, TenantPlan plan, TenantStatus status) {}
