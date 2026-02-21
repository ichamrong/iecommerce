package com.chamrong.iecommerce.auth.application.command;

/**
 * Command to authenticate an existing user.
 *
 * @param username the user's login name
 * @param password raw password to verify
 */
public record LoginCommand(String username, String password, String tenantId) {}
