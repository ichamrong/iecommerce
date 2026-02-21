package com.chamrong.iecommerce.auth;

/** Event published when a user is disabled. */
public record UserDisabledEvent(Long userId, String tenantId) {}
