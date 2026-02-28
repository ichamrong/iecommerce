package com.chamrong.iecommerce.auth.domain.event;

/** Event published when a tenant's preferences are updated. */
public record TenantPreferencesUpdatedEvent(String tenantCode) {}
