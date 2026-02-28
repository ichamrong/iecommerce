package com.chamrong.iecommerce.auth.application.query;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.UserSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Query handler that retrieves all active Keycloak sessions for the authenticated user.
 *
 * <p>Sessions are fetched from Keycloak Admin API via {@link IdentityService#listActiveSessions}.
 * No local DB involvement — sessions are Keycloak's source of truth.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListUserSessionsHandler {

  private final IdentityService identityService;

  /**
   * Returns all active sessions for a user identified by their Keycloak ID.
   *
   * @param keycloakId the user's Keycloak ID
   * @return list of active sessions (may be empty)
   */
  public @NonNull List<UserSession> handle(@NonNull final String keycloakId) {
    log.debug("Fetching active sessions for keycloakId={}", keycloakId);
    final List<UserSession> sessions = identityService.listActiveSessions(keycloakId);
    log.info("Found {} active session(s) for keycloakId={}", sessions.size(), keycloakId);
    return sessions;
  }
}
