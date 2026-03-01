package com.chamrong.iecommerce.audit.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Pure domain audit event: immutable, append-only. No framework dependencies.
 *
 * <p>Bank-grade: tenantId, correlationId, actor, target, eventType, outcome, severity, source,
 * optional ip/userAgent, metadata (size-limited, PII-scrubbed). Tamper-evidence via prevHash/hash
 * (hash chain) set by infrastructure.
 */
public final class AuditEvent {

  private final Long id;
  private final String tenantId;
  private final Instant createdAt;
  private final String correlationId;
  private final AuditActor actor;
  private final String eventType;
  private final AuditOutcome outcome;
  private final AuditSeverity severity;
  private final AuditTarget target;
  private final String sourceModule;
  private final String sourceEndpoint;
  private final String ipAddress;
  private final String userAgent;
  private final String metadataJson;
  private final String prevHash;
  private final String hash;

  private AuditEvent(Builder b) {
    this.id = b.id;
    this.tenantId = Objects.requireNonNull(b.tenantId, "tenantId");
    this.createdAt = Objects.requireNonNull(b.createdAt, "createdAt");
    this.correlationId = b.correlationId != null ? b.correlationId : "";
    this.actor = Objects.requireNonNull(b.actor, "actor");
    this.eventType = Objects.requireNonNull(b.eventType, "eventType");
    this.outcome = Objects.requireNonNull(b.outcome, "outcome");
    this.severity = Objects.requireNonNull(b.severity, "severity");
    this.target = Objects.requireNonNull(b.target, "target");
    this.sourceModule = b.sourceModule != null ? b.sourceModule : "";
    this.sourceEndpoint = b.sourceEndpoint != null ? b.sourceEndpoint : "";
    this.ipAddress = b.ipAddress;
    this.userAgent = b.userAgent;
    this.metadataJson = b.metadataJson;
    this.prevHash = b.prevHash;
    this.hash = b.hash;
  }

  public Long getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public AuditActor getActor() {
    return actor;
  }

  public String getEventType() {
    return eventType;
  }

  public AuditOutcome getOutcome() {
    return outcome;
  }

  public AuditSeverity getSeverity() {
    return severity;
  }

  public AuditTarget getTarget() {
    return target;
  }

  public String getSourceModule() {
    return sourceModule;
  }

  public String getSourceEndpoint() {
    return sourceEndpoint;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public String getMetadataJson() {
    return metadataJson;
  }

  public String getPrevHash() {
    return prevHash;
  }

  public String getHash() {
    return hash;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private Long id;
    private String tenantId;
    private Instant createdAt;
    private String correlationId;
    private AuditActor actor;
    private String eventType;
    private AuditOutcome outcome;
    private AuditSeverity severity;
    private AuditTarget target;
    private String sourceModule;
    private String sourceEndpoint;
    private String ipAddress;
    private String userAgent;
    private String metadataJson;
    private String prevHash;
    private String hash;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder tenantId(String tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder createdAt(Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder correlationId(String correlationId) {
      this.correlationId = correlationId;
      return this;
    }

    public Builder actor(AuditActor actor) {
      this.actor = actor;
      return this;
    }

    public Builder eventType(String eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder outcome(AuditOutcome outcome) {
      this.outcome = outcome;
      return this;
    }

    public Builder severity(AuditSeverity severity) {
      this.severity = severity;
      return this;
    }

    public Builder target(AuditTarget target) {
      this.target = target;
      return this;
    }

    public Builder sourceModule(String sourceModule) {
      this.sourceModule = sourceModule;
      return this;
    }

    public Builder sourceEndpoint(String sourceEndpoint) {
      this.sourceEndpoint = sourceEndpoint;
      return this;
    }

    public Builder ipAddress(String ipAddress) {
      this.ipAddress = ipAddress;
      return this;
    }

    public Builder userAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }

    public Builder metadataJson(String metadataJson) {
      this.metadataJson = metadataJson;
      return this;
    }

    public Builder prevHash(String prevHash) {
      this.prevHash = prevHash;
      return this;
    }

    public Builder hash(String hash) {
      this.hash = hash;
      return this;
    }

    public AuditEvent build() {
      return new AuditEvent(this);
    }
  }
}
