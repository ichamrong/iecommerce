package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import org.springframework.lang.NonNull;

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
  String registerUser(@NonNull RegisterCommand cmd);

  /**
   * Creates a user with a <strong>temporary</strong> password.
   *
   * <p>Keycloak will automatically add {@code UPDATE_PASSWORD} as a required action, forcing the
   * user to set a new password on first login.
   *
   * @return The unique IDP ID for the newly created user.
   */
  String createUserWithTemporaryPassword(@NonNull RegisterCommand cmd);

  /** Looks up the internal IDP ID for a username. */
  String lookupId(@NonNull String username);

  /** Authenticates a user and returns tokens. */
  AuthResponse authenticate(@NonNull LoginCommand cmd);

  /**
   * Updates a user's password (permanent — clears {@code UPDATE_PASSWORD} required action).
   *
   * <p>Used for self-service password changes.
   */
  void updatePassword(@NonNull String keycloakId, @NonNull String newPassword);

  /**
   * Resets a user's password to a temporary value (admin-triggered).
   *
   * <p>Keycloak sets {@code temporary=true}, which activates the {@code UPDATE_PASSWORD} required
   * action. The user must change the password on next login.
   */
  void resetPassword(@NonNull String keycloakId, @NonNull String temporaryPassword);

  /**
   * Triggers a Keycloak "Reset Password" email for the given user.
   *
   * <p>Keycloak sends the password-reset link directly from the realm's configured SMTP server.
   */
  void sendPasswordResetEmail(@NonNull String keycloakId);

  /**
   * Returns {@code true} if the user has {@code UPDATE_PASSWORD} as an active required action in
   * the IDP (i.e. they must change their password on next login).
   */
  boolean requiresPasswordChange(@NonNull String username);

  /**
   * Enables TOTP-based 2FA by adding the {@code CONFIGURE_TOTP} required action.
   *
   * <p>On next login, Keycloak prompts the user to set up an authenticator app (QR code).
   */
  void enableTotpForUser(@NonNull String keycloakId);

  /** Disables TOTP-based 2FA by removing all OTP credentials from the user's account. */
  void disableTotpForUser(@NonNull String keycloakId);

  /** Disables a user account in the IDP (no login allowed). */
  void disableUser(@NonNull String keycloakId);

  /** Re-enables a previously disabled user account. */
  void enableUser(@NonNull String keycloakId);

  // ── Session Management ─────────────────────────────────────────────────────

  /**
   * Returns all active sessions for the given user from the IDP.
   *
   * @param keycloakId the IDP-assigned user identifier
   * @return list of active sessions, never null (may be empty)
   */
  java.util.List<UserSession> listActiveSessions(@NonNull String keycloakId);

  /**
   * Revokes a single session by its session ID.
   *
   * <p>The user will be logged out of that session immediately.
   *
   * @param sessionId the Keycloak session identifier to revoke
   */
  void revokeSession(@NonNull String sessionId);

  /**
   * Revokes all active sessions for the given user.
   *
   * <p>Equivalent to "logout everywhere". The user must re-authenticate on all devices.
   *
   * @param keycloakId the IDP-assigned user identifier
   */
  void revokeAllSessions(@NonNull String keycloakId);

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
  void prepareWebAuthnSetup(@NonNull String keycloakId);
}
