package com.chamrong.iecommerce.auth.domain.idp;

/**
 * Supported external identity provider types.
 *
 * <p>Keycloak handles the OAuth2/OIDC/LDAP federation. Our system only needs to know the provider
 * alias so we can list available options for the frontend and build correct redirect hints.
 */
public enum IdentityProviderType {

  /** Keycloak-native (username/password or passkey). */
  KEYCLOAK,

  /** Google OAuth2 – requires Google IdP configured in Keycloak realm. */
  GOOGLE,

  /** GitHub OAuth2 – requires GitHub IdP configured in Keycloak realm. */
  GITHUB,

  /** Facebook OAuth2 – requires Facebook IdP configured in Keycloak realm. */
  FACEBOOK,

  /** Apple Sign In – requires Apple IdP configured in Keycloak realm. */
  APPLE,

  /** Microsoft Azure AD – requires Microsoft IdP configured in Keycloak realm. */
  MICROSOFT,

  /** X (Twitter) OAuth2 – requires X IdP configured in Keycloak realm. */
  X,

  /** LDAP / Active Directory – configured via Keycloak User Federation. */
  LDAP,

  /** Any other IDP not in this enum — treated generically. */
  OTHER
}
