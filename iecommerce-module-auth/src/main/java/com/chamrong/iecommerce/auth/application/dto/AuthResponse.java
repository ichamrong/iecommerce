package com.chamrong.iecommerce.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Standard OIDC Token Response returned on successful login via Keycloak Proxy.
 *
 * <p>The {@code requiresPasswordChange} flag is {@code true} when Keycloak has the {@code
 * UPDATE_PASSWORD} required action on the user's account (e.g. after an admin invite or
 * admin-triggered reset). The frontend must redirect the user to the change-password flow.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("expires_in") Integer expiresIn,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("session_state") String sessionState,
    @JsonProperty("requires_password_change") Boolean requiresPasswordChange) {

  /** Convenience constructor that matches the original Keycloak token response (no extra flag). */
  public AuthResponse(
      String accessToken,
      String refreshToken,
      Integer expiresIn,
      String tokenType,
      String sessionState) {
    this(accessToken, refreshToken, expiresIn, tokenType, sessionState, null);
  }

  /** Returns a copy with {@code requiresPasswordChange} set. */
  public AuthResponse withRequiresPasswordChange(boolean value) {
    return new AuthResponse(accessToken, refreshToken, expiresIn, tokenType, sessionState, value);
  }
}
