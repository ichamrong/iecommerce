package com.chamrong.iecommerce.auth.application.command.user;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Admin-only password reset (forced temporary reset).
 *
 * <p>Sets a temporary password in Keycloak. The user will be forced to change it on next login
 * ({@code UPDATE_PASSWORD} required action is activated automatically by Keycloak when {@code
 * temporary=true}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResetUserPasswordHandler {

  private final IdentityService identityService;

  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TENANT_ADMIN')")
  public void handle(String username, String newPassword) {
    String keycloakId = identityService.lookupId(username);
    identityService.resetPassword(keycloakId, newPassword);
    log.info("Admin reset password for user: {}. Temporary: true", username);
  }
}
