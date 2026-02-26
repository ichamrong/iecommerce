package com.chamrong.iecommerce.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "inventory_outbox_event")
@Getter
public class InventoryOutboxEvent {

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

  @Column(nullable = false)
  private int retryCount = 0;

  public static InventoryOutboxEvent pending(String tenantId, String eventType, String payload) {
    InventoryOutboxEvent e = new InventoryOutboxEvent();
    e.tenantId = tenantId;
    e.eventType = eventType;
    e.payload = payload;
    e.status = Status.PENDING;
    e.createdAt = Instant.now();
    return e;
  }

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
