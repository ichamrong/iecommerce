package com.chamrong.iecommerce.auth.domain.event;

import java.time.Instant;

/**
 * Domain event published when a new login is detected while the user already has active sessions.
 *
 * <p>Consumers of this event may choose to:
 *
 * <ul>
 *   <li>Send a security notification email to the user ("New sign-in from X").
 *   <li>Log a security audit trail entry.
 *   <li>Revoke previous sessions (enforce single-session policy).
 * </ul>
 *
 * @param username the affected username
 * @param tenantId the tenant scope
 * @param newSessionIp the IP address of the inbound login
 * @param existingSessionCount number of sessions already active at login time
 * @param occurredAt when the concurrent login was detected
 */
public record ConcurrentSessionDetectedEvent(
    String username,
    String tenantId,
    String newSessionIp,
    int existingSessionCount,
    Instant occurredAt) {

  public ConcurrentSessionDetectedEvent(
      final String username,
      final String tenantId,
      final String newSessionIp,
      final int existingSessionCount) {
    this(username, tenantId, newSessionIp, existingSessionCount, Instant.now());
  }
}
