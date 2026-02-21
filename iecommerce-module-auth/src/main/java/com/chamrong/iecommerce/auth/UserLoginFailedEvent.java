package com.chamrong.iecommerce.auth;

/** Event published when a user login fails. */
public record UserLoginFailedEvent(String username, String tenantId, String reason) {}
