package com.chamrong.iecommerce.auth.domain.event;

/** Event published when a user successfully logs in. */
public record UserLoggedInEvent(String username, String tenantId) {}
