package com.chamrong.iecommerce.auth.application.command.user;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Revokes all active sessions for a user — equivalent to "logout everywhere".
 *
 * <p>After this call, re-authentication is required on every device and browser. Useful for
 * security incidents (suspected account compromise) and account disablement.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevokeAllSessionsHandler {

  private final IdentityService identityService;

  /**
   * Revokes all active sessions for the given user.
   *
   * @param keycloakId the user's Keycloak ID
   */
  public void handle(@NonNull final String keycloakId) {
    log.warn("Revoking ALL sessions for keycloakId={} (logout-everywhere)", keycloakId);
    identityService.revokeAllSessions(keycloakId);
    log.info("All sessions revoked for keycloakId={}", keycloakId);
  }
}
