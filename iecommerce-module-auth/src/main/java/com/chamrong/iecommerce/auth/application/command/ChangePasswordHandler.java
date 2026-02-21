package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.infrastructure.init.KeycloakProperties;
import com.chamrong.iecommerce.common.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordHandler {

  private final Keycloak keycloak;
  private final KeycloakProperties properties;
  private final LoginUserHandler loginUserHandler;

  public void handle(ChangePasswordCommand cmd) {
    var username = SecurityContextHolder.getContext().getAuthentication().getName();
    var tenantId = TenantContext.requireTenantId();

    // 1. Verify current password by attempting an internal login
    try {
      loginUserHandler.handle(new LoginCommand(username, cmd.currentPassword(), tenantId));
    } catch (Exception e) {
      throw new BadCredentialsException("Invalid current password");
    }

    // 2. Update password in Keycloak
    var realm = keycloak.realm(properties.getRealm());
    var users = realm.users().search(username, true);
    if (users.isEmpty()) {
      throw new IllegalStateException("User not found in identity provider: " + username);
    }
    var keycloakId = users.get(0).getId();

    var credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(cmd.newPassword());
    credential.setTemporary(false);

    realm.users().get(keycloakId).resetPassword(credential);
    log.info("Password changed successfully for user: {}", username);
  }
}
