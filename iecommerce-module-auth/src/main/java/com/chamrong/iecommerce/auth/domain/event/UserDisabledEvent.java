package com.chamrong.iecommerce.auth.domain.event;

/** Event published when a user is disabled. */
public record UserDisabledEvent(Long userId, String tenantId) {}
