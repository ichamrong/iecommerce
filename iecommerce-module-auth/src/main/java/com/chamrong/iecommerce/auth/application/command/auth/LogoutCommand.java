package com.chamrong.iecommerce.auth.application.command.auth;

/** Command to logout a user. Refresh token may come from request body or from httpOnly cookie. */
public record LogoutCommand(String refreshToken) {}
