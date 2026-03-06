package com.chamrong.iecommerce.auth.application.command;

import jakarta.validation.constraints.NotBlank;

/**
 * Command payload for changing both username and password in a single first-login flow.
 *
 * <p>The current password is required to prevent credential stuffing and ensure that only the
 * authenticated user can update their own credentials.
 */
public record ChangeCredentialsCommand(
    @NotBlank String currentPassword, @NotBlank String newUsername, @NotBlank String newPassword) {}
