package com.chamrong.iecommerce.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Standard OIDC Token Response returned on successful registration or login via Keycloak Proxy. */
public record AuthResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("expires_in") Integer expiresIn,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("session_state") String sessionState) {}
