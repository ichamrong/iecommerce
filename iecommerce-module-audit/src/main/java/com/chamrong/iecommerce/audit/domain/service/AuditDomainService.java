package com.chamrong.iecommerce.audit.domain.service;

import com.chamrong.iecommerce.audit.domain.model.AuditEvent;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Domain service for audit: canonical representation and hash computation (for tamper-evidence hash
 * chain). No I/O; pure functions.
 */
public final class AuditDomainService {

  private static final String SHA256 = "SHA-256";

  private AuditDomainService() {}

  /**
   * Builds a canonical string from event fields for hashing. Order and format must be stable.
   *
   * @param event event (id may be null for new records)
   * @return canonical string
   */
  public static String toCanonicalForm(AuditEvent event) {
    StringBuilder sb = new StringBuilder();
    sb.append(event.getTenantId()).append("|");
    sb.append(event.getCreatedAt()).append("|");
    sb.append(event.getCorrelationId()).append("|");
    sb.append(event.getActor().actorId())
        .append("|")
        .append(event.getActor().actorType())
        .append("|")
        .append(event.getActor().role())
        .append("|");
    sb.append(event.getEventType()).append("|");
    sb.append(event.getOutcome()).append("|");
    sb.append(event.getSeverity()).append("|");
    sb.append(event.getTarget().targetType())
        .append("|")
        .append(event.getTarget().targetId())
        .append("|");
    sb.append(event.getSourceModule()).append("|").append(event.getSourceEndpoint()).append("|");
    sb.append(event.getIpAddress() != null ? event.getIpAddress() : "").append("|");
    sb.append(event.getUserAgent() != null ? event.getUserAgent() : "").append("|");
    sb.append(event.getMetadataJson() != null ? event.getMetadataJson() : "").append("|");
    sb.append(event.getPrevHash() != null ? event.getPrevHash() : "");
    return sb.toString();
  }

  /**
   * Computes SHA-256 hex hash of (canonicalForm). Used for hash chain: hash(current_fields +
   * prevHash).
   *
   * @param canonicalForm output of toCanonicalForm
   * @return lowercase hex string
   */
  public static String computeHash(String canonicalForm) {
    try {
      MessageDigest md = MessageDigest.getInstance(SHA256);
      byte[] digest = md.digest(canonicalForm.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
