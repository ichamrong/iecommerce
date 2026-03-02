package com.chamrong.iecommerce.customer.domain.ports;

/**
 * Port for customer credential verification and token generation (e.g. Keycloak or local auth).
 * Used by login flow.
 */
public interface CustomerCredentialPort {

  boolean verify(String customerId, String password);

  /** Generate tokens after successful login; sessionId is stored for revocation. */
  AuthTokens generateTokens(String customerId, long tokenVersion, String sessionId);

  /** DTO for auth token response. */
  record AuthTokens(String accessToken, String refreshToken) {}
}
