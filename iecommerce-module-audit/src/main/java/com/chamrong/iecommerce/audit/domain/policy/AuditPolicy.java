package com.chamrong.iecommerce.audit.domain.policy;

/**
 * Policy constants for audit: metadata size limit, PII handling, and what must be logged.
 *
 * <p>No PII stored unless explicitly allowed and masked. Payload size limits enforced at
 * application layer.
 */
public final class AuditPolicy {

  /** Maximum size in bytes for metadata_json. */
  public static final int METADATA_JSON_MAX_BYTES = 8192;

  /** Maximum length for ip_address (e.g. IPv6 + scope). */
  public static final int IP_ADDRESS_MAX_LENGTH = 45;

  /** Maximum length for user_agent. */
  public static final int USER_AGENT_MAX_LENGTH = 500;

  private AuditPolicy() {}
}
