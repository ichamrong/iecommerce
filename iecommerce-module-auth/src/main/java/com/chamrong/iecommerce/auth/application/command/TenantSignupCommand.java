package com.chamrong.iecommerce.auth.application.command;

/**
 * Self-service tenant registration command.
 *
 * @param shopName Display name of the store — also used to generate the tenant code slug
 * @param ownerUsername Username for the owner account
 * @param ownerEmail Email for the owner account
 * @param ownerPassword Plain-text password (will be hashed)
 */
public record TenantSignupCommand(
    String shopName, String ownerUsername, String ownerEmail, String ownerPassword) {}
