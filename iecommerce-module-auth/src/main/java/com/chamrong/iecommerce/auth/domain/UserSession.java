package com.chamrong.iecommerce.auth.domain;

import java.time.Instant;

/**
 * Represents an active Keycloak session for a user.
 *
 * <p>Read-only value object — not persisted locally. Retrieved from Keycloak Admin API via {@link
 * IdentityService#listActiveSessions(String)}.
 *
 * @param sessionId Keycloak-assigned session identifier.
 * @param ipAddress Client IP address at session start.
 * @param browser User-agent / browser string (may be empty for machine clients).
 * @param startedAt When the session was created.
 * @param lastAccessedAt When the session was last used (rolling timestamp).
 */
public record UserSession(
    String sessionId, String ipAddress, String browser, Instant startedAt, Instant lastAccessedAt) {

  public UserSession {
    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("sessionId must not be blank");
    }
  }
}
