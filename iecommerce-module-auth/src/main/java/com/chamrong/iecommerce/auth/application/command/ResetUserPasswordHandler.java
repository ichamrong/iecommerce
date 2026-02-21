package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.infrastructure.init.KeycloakProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetUserPasswordHandler {

  private final Keycloak keycloak;
  private final KeycloakProperties properties;

  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TENANT_ADMIN')")
  public void handle(String username, String newPassword) {
    var realm = keycloak.realm(properties.getRealm());
    var users = realm.users().search(username, true);
    if (users.isEmpty()) {
      throw new IllegalStateException("User not found in identity provider: " + username);
    }
    var keycloakId = users.get(0).getId();

    var credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(newPassword);
    credential.setTemporary(true); // Force change on next login

    realm.users().get(keycloakId).resetPassword(credential);
    log.info("Admin reset password for user: {}. Temporary: true", username);
  }
}
