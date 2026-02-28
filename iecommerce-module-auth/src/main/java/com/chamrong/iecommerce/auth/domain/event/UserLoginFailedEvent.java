package com.chamrong.iecommerce.auth.domain.event;

/** Event published when a user login fails. */
public record UserLoginFailedEvent(String username, String tenantId, String reason) {}
