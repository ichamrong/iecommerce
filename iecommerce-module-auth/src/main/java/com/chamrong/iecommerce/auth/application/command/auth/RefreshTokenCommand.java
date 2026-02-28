package com.chamrong.iecommerce.auth.application.command.auth;

import jakarta.validation.constraints.NotBlank;

/** Command to refresh an access token. */
public record RefreshTokenCommand(
    @NotBlank(message = "Refresh token is required") String refreshToken) {}
