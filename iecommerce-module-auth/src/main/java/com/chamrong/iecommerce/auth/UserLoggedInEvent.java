package com.chamrong.iecommerce.auth;

/** Event published when a user successfully logs in. */
public record UserLoggedInEvent(String username, String tenantId) {}
