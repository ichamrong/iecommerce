package com.chamrong.iecommerce.auth.application.command.password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Command to initiate a forgotten-password reset email.
 *
 * <p>Always succeeds (200 OK) regardless of whether the email exists — prevents user enumeration
 * (OWASP A07).
 */
public record ForgotPasswordCommand(@NotBlank @Email String email, @NotBlank String tenantId) {}
