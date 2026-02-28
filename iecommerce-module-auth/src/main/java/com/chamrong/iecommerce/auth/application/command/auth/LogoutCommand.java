package com.chamrong.iecommerce.auth.application.command.auth;

import jakarta.validation.constraints.NotBlank;

/** Command to logout a user using their refresh token. */
public record LogoutCommand(@NotBlank(message = "Refresh token is required") String refreshToken) {}
