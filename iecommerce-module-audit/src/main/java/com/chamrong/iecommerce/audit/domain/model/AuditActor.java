package com.chamrong.iecommerce.audit.domain.model;

import java.util.Objects;

/**
 * Actor who performed the audited action: identifier, type, and optional role.
 *
 * @param actorId stable identifier (e.g. user UUID, "SYSTEM")
 * @param actorType e.g. USER, SYSTEM, SERVICE
 * @param role optional role at time of action
 */
public record AuditActor(String actorId, String actorType, String role) {

  public AuditActor {
    Objects.requireNonNull(actorId, "actorId");
    Objects.requireNonNull(actorType, "actorType");
    role = role != null ? role : "";
  }

  public static AuditActor system() {
    return new AuditActor("SYSTEM", "SYSTEM", "");
  }

  public static AuditActor user(String userId, String role) {
    return new AuditActor(userId, "USER", role != null ? role : "");
  }
}
