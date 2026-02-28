package com.chamrong.iecommerce.auth.domain.event;

/**
 * Event published when a new user is successfully registered.
 *
 * @param userId the generated internal user ID
 * @param username the chosen username
 * @param email the chosen email
 * @param tenantId the tenant this user belongs to
 */
public record UserRegisteredEvent(Long userId, String username, String email, String tenantId) {}
