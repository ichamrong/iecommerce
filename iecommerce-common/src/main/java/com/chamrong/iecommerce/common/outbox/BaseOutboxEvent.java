package com.chamrong.iecommerce.common.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class BaseOutboxEvent {

  public enum Status {
    PENDING,
    SENT,
    FAILED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String tenantId;

  @Column(nullable = false, length = 100)
  private String eventType;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status = Status.PENDING;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  private Instant processedAt;

  private int retryCount = 0;

  // ── Protected setters — for use by subclass factory methods only ─────────

  protected void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  protected void setEventType(String eventType) {
    this.eventType = eventType;
  }

  protected void setPayload(String payload) {
    this.payload = payload;
  }

  protected void setStatus(Status status) {
    this.status = status;
  }

  protected void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  // ── Domain behaviour ─────────────────────────────────────────────────────

  public void markSent() {
    this.status = Status.SENT;
    this.processedAt = Instant.now();
  }

  public void markFailed() {
    this.status = Status.FAILED;
    this.retryCount++;
    this.processedAt = Instant.now();
  }
}
