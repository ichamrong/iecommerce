package com.chamrong.iecommerce.common.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
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
