package com.chamrong.iecommerce.auth.application.command.auth;

/** Command to refresh an access token. Token may come from request body or from httpOnly cookie. */
public record RefreshTokenCommand(String refreshToken) {}
