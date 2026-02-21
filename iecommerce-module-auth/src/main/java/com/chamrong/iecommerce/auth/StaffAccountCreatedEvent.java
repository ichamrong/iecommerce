package com.chamrong.iecommerce.auth;

/**
 * Event published when a new platform staff member is created.
 *
 * <p>The auth module should listen for this event to create the corresponding User account.
 */
public record StaffAccountCreatedEvent(
    String username, String email, String temporaryPassword, String fullName, String department) {}
