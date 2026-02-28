package com.chamrong.iecommerce.auth.domain;

/**
 * Canonical enum for Keycloak required-action identifiers.
 *
 * <p>Centralizes all magic strings used when interacting with Keycloak's {@code
 * executeActionsEmail} and {@code requiredActions} APIs. Using an enum prevents typos and makes
 * refactoring safe.
 */
public enum KeycloakAction {

  /** Forces the user to choose a new password on next login. */
  UPDATE_PASSWORD,

  /** Forces the user to configure a TOTP authenticator app on next login. */
  CONFIGURE_TOTP,

  /** Forces the user to verify their email address on next login. */
  VERIFY_EMAIL,

  /**
   * Forces the user to register a WebAuthn (Passkey) credential on next login. Requires Keycloak ≥
   * 21 with WebAuthn authenticator configured in the realm.
   */
  CONFIGURE_WEBAUTHN,

  /** Forces the user to update their profile information on next login. */
  UPDATE_PROFILE;

  /**
   * Returns the exact string Keycloak expects in its API calls. This is always the enum name itself
   * — using {@link #name()} directly.
   */
  public String keycloakValue() {
    return name();
  }
}
