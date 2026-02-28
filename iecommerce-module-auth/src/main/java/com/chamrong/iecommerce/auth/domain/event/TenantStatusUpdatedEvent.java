package com.chamrong.iecommerce.auth.domain.event;

import com.chamrong.iecommerce.auth.domain.TenantStatus;

/** Event published when a tenant's status is updated. */
public record TenantStatusUpdatedEvent(String tenantCode, TenantStatus status) {}
