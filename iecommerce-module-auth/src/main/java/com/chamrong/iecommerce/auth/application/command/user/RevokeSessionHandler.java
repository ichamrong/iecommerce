package com.chamrong.iecommerce.auth.application.command.user;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Revokes a single user session in Keycloak.
 *
 * <p>The session is immediately invalidated — the user will need to re-authenticate on the affected
 * device on their next request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevokeSessionHandler {

  private final IdentityService identityService;

  /**
   * Revokes the specified session.
   *
   * @param sessionId the Keycloak session identifier to revoke
   * @param keycloakId the user's Keycloak ID (used for audit logging)
   */
  public void handle(@NonNull final String sessionId, @NonNull final String keycloakId) {
    log.info("Revoking sessionId={} for keycloakId={}", sessionId, keycloakId);
    identityService.revokeSession(sessionId);
    log.info("Session revoked successfully sessionId={}", sessionId);
  }
}
