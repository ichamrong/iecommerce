package com.chamrong.iecommerce.auth.application.command.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Command for a platform/tenant admin to create a new user account.
 *
 * <p>The user is created with a temporary password ({@code temporary=true} in Keycloak), which
 * forces them to set a new password on first login. An invitation email with the reset link is sent
 * automatically via Keycloak's Execute-Actions-Email.
 */
public record AdminCreateUserCommand(
    @NotBlank String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 72) String temporaryPassword,
    @NotBlank String tenantId,
    String role) {}
