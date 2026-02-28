package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.common.annotation.Masked;

/**
 * Command to authenticate an existing user.
 *
 * @param username the user's login name
 * @param password raw password to verify
 */
public record LoginCommand(String username, @Masked String password, String tenantId) {}
