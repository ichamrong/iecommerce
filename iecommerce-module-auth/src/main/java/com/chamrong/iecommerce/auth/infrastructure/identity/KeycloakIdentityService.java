package com.chamrong.iecommerce.auth.infrastructure.identity;

import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Keycloak-backed implementation of {@link IdentityService}.
 *
 * <p>All IDP interactions are isolated here. The rest of the application only depends on the {@link
 * IdentityService} interface, keeping the domain clean of Keycloak types.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakIdentityService implements IdentityService {

  private static final String UPDATE_PASSWORD_ACTION = "UPDATE_PASSWORD";
  private static final String CONFIGURE_TOTP_ACTION = "CONFIGURE_TOTP";
  private static final String OTP_CREDENTIAL_TYPE = "otp";

  private final Keycloak keycloak;
  private final KeycloakProperties properties;
  private final RestClient restClient = RestClient.builder().build();

  // ─── User Registration ────────────────────────────────────────────────────

  @Override
  public String registerUser(RegisterCommand cmd) {
    return createKeycloakUser(cmd, false);
  }

  @Override
  public String createUserWithTemporaryPassword(RegisterCommand cmd) {
    return createKeycloakUser(cmd, true);
  }

  /**
   * Creates a user in Keycloak.
   *
   * @param temporary if {@code true}, the credential is marked temporary and Keycloak automatically
   *     adds {@code UPDATE_PASSWORD} as a required action, forcing a reset on first login.
   */
  private String createKeycloakUser(RegisterCommand cmd, boolean temporary) {
    var realmResource = keycloak.realm(properties.getRealm());

    var userRep = new UserRepresentation();
    userRep.setUsername(cmd.username());
    userRep.setEmail(cmd.email());
    userRep.setEnabled(true);
    userRep.setEmailVerified(!temporary); // unverified for invited users (email flow verifies)
    userRep.setAttributes(Map.of("tenantId", List.of(cmd.tenantId())));

    var credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(cmd.password());
    credential.setTemporary(temporary);
    userRep.setCredentials(List.of(credential));

    try (var response = realmResource.users().create(userRep)) {
      if (response.getStatus() == 201 || response.getStatus() == 200) {
        return response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
      } else if (response.getStatus() == 409) {
        throw new RuntimeException("User already exists in Identity Provider.");
      } else {
        log.error("Failed to create user in Keycloak. Status: {}", response.getStatus());
        throw new RuntimeException("Failed to register user in Identity Provider.");
      }
    }
  }

  // ─── Lookup ───────────────────────────────────────────────────────────────

  @Override
  @Cacheable(value = "identity_ids", key = "#username")
  public String lookupId(String username) {
    var users = keycloak.realm(properties.getRealm()).users().search(username, true);
    if (users.isEmpty()) {
      throw new IllegalStateException("User not found in identity provider: " + username);
    }
    return users.getFirst().getId();
  }

  // ─── Authentication ───────────────────────────────────────────────────────

  @Override
  public AuthResponse authenticate(LoginCommand cmd) {
    String tokenUrl =
        properties.getServerUrl()
            + "/realms/"
            + properties.getRealm()
            + "/protocol/openid-connect/token";

    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("client_id", properties.getClients().getWeb());
    formData.add("grant_type", "password");
    formData.add("username", cmd.username());
    formData.add("password", cmd.password());

    try {
      var response =
          restClient
              .post()
              .uri(tokenUrl)
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .body(formData)
              .retrieve()
              .body(AuthResponse.class);

      if (response == null) {
        throw new BadCredentialsException("Invalid credentials or user not found.");
      }
      return response;
    } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest e) {
      throw new BadCredentialsException("Invalid credentials or user not found.");
    }
  }

  // ─── Password Management ──────────────────────────────────────────────────

  @Override
  public void updatePassword(String keycloakId, String newPassword) {
    var credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(newPassword);
    credential.setTemporary(false); // permanent — clears UPDATE_PASSWORD action

    keycloak.realm(properties.getRealm()).users().get(keycloakId).resetPassword(credential);
  }

  @Override
  public void resetPassword(String keycloakId, String temporaryPassword) {
    var credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(temporaryPassword);
    credential.setTemporary(true); // activates UPDATE_PASSWORD required action

    keycloak.realm(properties.getRealm()).users().get(keycloakId).resetPassword(credential);
  }

  @Override
  public void sendPasswordResetEmail(String keycloakId) {
    // Triggers Keycloak to send its built-in password-reset email.
    // Requires the realm to have SMTP configured.
    keycloak
        .realm(properties.getRealm())
        .users()
        .get(keycloakId)
        .executeActionsEmail(List.of(UPDATE_PASSWORD_ACTION));
    log.info("Password reset email triggered for Keycloak user '{}'.", keycloakId);
  }

  @Override
  public boolean requiresPasswordChange(String username) {
    var users = keycloak.realm(properties.getRealm()).users().search(username, true);
    if (users.isEmpty()) {
      return false;
    }
    List<String> requiredActions = users.getFirst().getRequiredActions();
    return requiredActions != null && requiredActions.contains(UPDATE_PASSWORD_ACTION);
  }

  // ─── 2FA / TOTP ───────────────────────────────────────────────────────────

  @Override
  public void enableTotpForUser(String keycloakId) {
    // Adds CONFIGURE_TOTP to the user's required actions.
    // On next login, Keycloak will prompt the user to set up an authenticator app.
    keycloak
        .realm(properties.getRealm())
        .users()
        .get(keycloakId)
        .executeActionsEmail(List.of(CONFIGURE_TOTP_ACTION));
    log.info("CONFIGURE_TOTP required action added for Keycloak user '{}'.", keycloakId);
  }

  @Override
  public void disableTotpForUser(String keycloakId) {
    var userResource = keycloak.realm(properties.getRealm()).users().get(keycloakId);

    // Remove all OTP credentials
    userResource.credentials().stream()
        .filter(c -> OTP_CREDENTIAL_TYPE.equals(c.getType()))
        .forEach(c -> userResource.removeCredential(c.getId()));

    // Clear any pending CONFIGURE_TOTP required action
    var userRep = userResource.toRepresentation();
    if (userRep.getRequiredActions() != null) {
      userRep.getRequiredActions().remove(CONFIGURE_TOTP_ACTION);
      userResource.update(userRep);
    }

    log.info("2FA disabled for Keycloak user '{}'.", keycloakId);
  }

  // ─── Account Status ───────────────────────────────────────────────────────

  @Override
  public void disableUser(String keycloakId) {
    var user = new UserRepresentation();
    user.setEnabled(false);
    keycloak.realm(properties.getRealm()).users().get(keycloakId).update(user);
  }

  @Override
  public void enableUser(String keycloakId) {
    var user = new UserRepresentation();
    user.setEnabled(true);
    keycloak.realm(properties.getRealm()).users().get(keycloakId).update(user);
  }

  // ─── Session Management ───────────────────────────────────────────────────

  @Override
  public List<com.chamrong.iecommerce.auth.domain.UserSession> listActiveSessions(
      final String keycloakId) {
    return keycloak.realm(properties.getRealm()).users().get(keycloakId).getUserSessions().stream()
        .map(
            s ->
                new com.chamrong.iecommerce.auth.domain.UserSession(
                    s.getId(),
                    s.getIpAddress(),
                    s.getClients() != null
                        ? s.getClients().values().stream().findFirst().orElse("")
                        : "",
                    java.time.Instant.ofEpochMilli(s.getStart()),
                    java.time.Instant.ofEpochMilli(s.getLastAccess())))
        .toList();
  }

  @Override
  public void revokeSession(final String sessionId) {
    keycloak.realm(properties.getRealm()).deleteSession(sessionId, false);
    log.info("Revoked Keycloak session '{}'", sessionId);
  }

  @Override
  public void revokeAllSessions(final String keycloakId) {
    keycloak.realm(properties.getRealm()).users().get(keycloakId).logout();
    log.info("Revoked all Keycloak sessions for user '{}'", keycloakId);
  }

  // ─── Social / Federated Identity Providers ────────────────────────────────

  @Override
  public List<com.chamrong.iecommerce.auth.domain.idp.SocialProvider> listSocialProviders() {
    return keycloak.realm(properties.getRealm()).identityProviders().findAll().stream()
        .filter(idp -> Boolean.TRUE.equals(idp.isEnabled()))
        .map(
            idp ->
                new com.chamrong.iecommerce.auth.domain.idp.SocialProvider(
                    resolveProviderType(idp.getProviderId()),
                    idp.getAlias(),
                    idp.getDisplayName() != null ? idp.getDisplayName() : idp.getAlias(),
                    true))
        .toList();
  }

  private static com.chamrong.iecommerce.auth.domain.idp.IdentityProviderType resolveProviderType(
      final String providerId) {
    if (providerId == null) {
      return com.chamrong.iecommerce.auth.domain.idp.IdentityProviderType.OTHER;
    }
    return switch (providerId.toLowerCase()) {
      case "google" -> com.chamrong.iecommerce.auth.domain.idp.IdentityProviderType.GOOGLE;
      case "github" -> com.chamrong.iecommerce.auth.domain.idp.IdentityProviderType.GITHUB;
      case "facebook" -> com.chamrong.iecommerce.auth.domain.idp.IdentityProviderType.FACEBOOK;
      case "apple" -> com.chamrong.iecommerce.auth.domain.idp.IdentityProviderType.APPLE;
      case "microsoft" -> com.chamrong.iecommerce.auth.domain.idp.IdentityProviderType.MICROSOFT;
      case "twitter", "x" -> com.chamrong.iecommerce.auth.domain.idp.IdentityProviderType.X;
      case "ldap", "active-directory" ->
          com.chamrong.iecommerce.auth.domain.idp.IdentityProviderType.LDAP;
      default -> com.chamrong.iecommerce.auth.domain.idp.IdentityProviderType.OTHER;
    };
  }

  // ─── WebAuthn / Passkey ───────────────────────────────────────────────────

  @Override
  public void prepareWebAuthnSetup(final String keycloakId) {
    keycloak
        .realm(properties.getRealm())
        .users()
        .get(keycloakId)
        .executeActionsEmail(
            List.of(
                com.chamrong.iecommerce.auth.domain.KeycloakAction.CONFIGURE_WEBAUTHN
                    .keycloakValue()));
    log.info("WebAuthn setup email triggered for Keycloak user '{}'", keycloakId);
  }
}
