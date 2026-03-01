package com.chamrong.iecommerce.payment.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.payment.domain.ProviderType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "payment_webhook_event",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_webhook_event",
          columnNames = {"provider", "provider_event_id"})
    })
public class WebhookEventEntity {

  protected WebhookEventEntity() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ProviderType getProvider() {
    return provider;
  }

  public void setProvider(ProviderType provider) {
    this.provider = provider;
  }

  public String getProviderEventId() {
    return providerEventId;
  }

  public void setProviderEventId(String providerEventId) {
    this.providerEventId = providerEventId;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getRawPayload() {
    return rawPayload;
  }

  public void setRawPayload(String rawPayload) {
    this.rawPayload = rawPayload;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getProcessedAt() {
    return processedAt;
  }

  public void setProcessedAt(Instant processedAt) {
    this.processedAt = processedAt;
  }

  public String getPayloadHash() {
    return payloadHash;
  }

  public void setPayloadHash(String payloadHash) {
    this.payloadHash = payloadHash;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public WebhookEventEntity(
      ProviderType provider,
      String providerEventId,
      String eventType,
      String rawPayload,
      String payloadHash) {
    this.provider = Objects.requireNonNull(provider);
    this.providerEventId = Objects.requireNonNull(providerEventId);
    this.eventType = Objects.requireNonNull(eventType);
    this.rawPayload = rawPayload;
    this.payloadHash = payloadHash;
    this.createdAt = Instant.now();
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private ProviderType provider;

  @Column(name = "provider_event_id", nullable = false, length = 255)
  private String providerEventId;

  @Column(name = "event_type", nullable = false, length = 100)
  private String eventType;

  @Column(name = "raw_payload", columnDefinition = "TEXT")
  private String rawPayload;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "processed_at")
  private Instant processedAt;

  @Column(name = "payload_hash", length = 64)
  private String payloadHash;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o))
      return false;
    WebhookEventEntity that = (WebhookEventEntity) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
