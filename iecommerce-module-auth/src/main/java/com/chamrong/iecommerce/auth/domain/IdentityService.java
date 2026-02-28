package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;

/**
 * Abstraction for Identity Provider operations.
 *
 * <p>Decouples the domain and application layers from any specific IDP implementation (Keycloak,
 * Auth0, etc.). All methods that interact with the IDP must go through this interface.
 */
public interface IdentityService {

  /**
   * Registers a new user with a permanent password.
   *
   * @return The unique ID assigned by the IDP.
   */
  String registerUser(RegisterCommand cmd);

  /**
   * Creates a user with a <strong>temporary</strong> password.
   *
   * <p>Keycloak will automatically add {@code UPDATE_PASSWORD} as a required action, forcing the
   * user to set a new password on first login.
   *
   * @return The unique IDP ID for the newly created user.
   */
  String createUserWithTemporaryPassword(RegisterCommand cmd);

  /** Looks up the internal IDP ID for a username. */
  String lookupId(String username);

  /**
   * Authenticate a user via username and password.
   *
   * @param cmd login credentials
   * @return standard IDP token response
   */
  AuthResponse authenticate(com.chamrong.iecommerce.auth.application.command.LoginCommand cmd);

  /**
   * Refreshes an access token using a valid refresh token.
   *
   * @param refreshToken the active refresh token
   * @return a new AuthResponse with access token and refresh token
   */
  AuthResponse refreshToken(String refreshToken);

  /**
   * Logs out a user using their refresh token via the Identity Provider.
   *
   * @param refreshToken the active refresh token to invalidate
   */
  void logout(String refreshToken);

  /**
   * Updates a user's password (permanent — clears {@code UPDATE_PASSWORD} required action).
   *
   * <p>Used for self-service password changes.
   */
  void updatePassword(String keycloakId, String newPassword);

  /**
   * Resets a user's password to a temporary value (admin-triggered).
   *
   * <p>Keycloak sets {@code temporary=true}, which activates the {@code UPDATE_PASSWORD} required
   * action. The user must change the password on next login.
   */
  void resetPassword(String keycloakId, String temporaryPassword);

  /**
   * Triggers a Keycloak "Reset Password" email for the given user.
   *
   * <p>Keycloak sends the password-reset link directly from the realm's configured SMTP server.
   */
  void sendPasswordResetEmail(String keycloakId);

  /**
   * Returns {@code true} if the user has {@code UPDATE_PASSWORD} as an active required action in
   * the IDP (i.e. they must change their password on next login).
   */
  boolean requiresPasswordChange(String username);

  /**
   * Enables TOTP-based 2FA by adding the {@code CONFIGURE_TOTP} required action.
   *
   * <p>On next login, Keycloak prompts the user to set up an authenticator app (QR code).
   */
  void enableTotpForUser(String keycloakId);

  /** Disables TOTP-based 2FA by removing all OTP credentials from the user's account. */
  void disableTotpForUser(String keycloakId);

  /** Disables a user account in the IDP (no login allowed). */
  void disableUser(String keycloakId);

  /** Re-enables a previously disabled user account. */
  void enableUser(String keycloakId);

  /**
   * Unlocks a user account in the IDP (clears brute-force failure counts).
   *
   * @param keycloakId the IDP-assigned user identifier
   */
  void unlockUser(String keycloakId);

  /**
   * Triggers a Keycloak "Verify Email" email for the given user.
   *
   * @param keycloakId the IDP-assigned user identifier
   */
  void sendVerificationEmail(String keycloakId);

  // ── Session Management ─────────────────────────────────────────────────────

  /**
   * Returns all active sessions for the given user from the IDP.
   *
   * @param keycloakId the IDP-assigned user identifier
   * @return list of active sessions, never null (may be empty)
   */
  java.util.List<UserSession> listActiveSessions(String keycloakId);

  /**
   * Revokes a single session by its session ID.
   *
   * <p>The user will be logged out of that session immediately.
   *
   * @param sessionId the Keycloak session identifier to revoke
   */
  void revokeSession(String sessionId);

  /**
   * Revokes all active sessions for the given user.
   *
   * <p>Equivalent to "logout everywhere". The user must re-authenticate on all devices.
   *
   * @param keycloakId the IDP-assigned user identifier
   */
  void revokeAllSessions(String keycloakId);

  // ── Social / Federated Identity Providers ─────────────────────────────────

  /**
   * Lists all identity providers enabled in the Keycloak realm (Google, GitHub, LDAP, etc.).
   *
   * <p>Cached — use the {@code social_providers} cache to avoid repeated Admin API calls.
   *
   * @return list of enabled social providers, never null
   */
  java.util.List<com.chamrong.iecommerce.auth.domain.idp.SocialProvider> listSocialProviders();

  // ── WebAuthn / Passkey ─────────────────────────────────────────────────────

  /**
   * Adds the {@code CONFIGURE_WEBAUTHN} required action to the user.
   *
   * <p>On next login, Keycloak prompts the user to register a passkey (biometric or hardware key).
   * Requires Keycloak ≥ 21 with WebAuthn authenticator configured in the realm.
   *
   * @param keycloakId the IDP-assigned user identifier
   */
  void prepareWebAuthnSetup(String keycloakId);
}
