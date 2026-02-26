package com.chamrong.iecommerce.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Outbox Pattern — guarantees at-least-once event delivery without dual-write risk.
 *
 * <p><b>The Problem it Solves (Banking Context):</b><br>
 * If you write to the DB AND publish to a message broker in the same request, one can succeed and
 * the other can fail. Result: your DB says "order completed" but loyalty points were never awarded,
 * or vice-versa. This is called the "dual-write problem."
 *
 * <p><b>The Solution:</b><br>
 * 1. Within the same ACID transaction that saves the Order, we also insert a row into this outbox
 * table. Both writes succeed or both fail — atomically.<br>
 * 2. A background {@link com.chamrong.iecommerce.order.infrastructure.OutboxRelayScheduler} polls
 * the outbox every few seconds, publishes any PENDING events, then marks them SENT.<br>
 * 3. If the server crashes after commit but before sending, the row remains PENDING and will be
 * retried on restart. This is called "at-least-once delivery."
 *
 * <p>PostgreSQL's WAL (Write-Ahead Log) is the underlying hardware guarantee that backs this: even
 * a power loss after a DB commit won't lose the outbox row.
 */
@Entity
@Table(name = "order_outbox_event")
public class OrderOutboxEvent {

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

  /** Logical event type, e.g. "OrderCompletedEvent", "OrderCancelledEvent" */
  @Column(nullable = false, length = 100)
  private String eventType;

  /** JSON payload serialized from the domain event object. */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status = Status.PENDING;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  private Instant processedAt;

  /** How many times delivery was attempted (for monitoring/alerting). */
  @Column(nullable = false)
  private int retryCount = 0;

  // ── Factory ───────────────────────────────────────────────────────────────

  public static OrderOutboxEvent pending(String tenantId, String eventType, String payload) {
    OrderOutboxEvent e = new OrderOutboxEvent();
    e.tenantId = tenantId;
    e.eventType = eventType;
    e.payload = payload;
    e.status = Status.PENDING;
    e.createdAt = Instant.now();
    return e;
  }

  // ── Mutations (allowed — lifecycle of the outbox row) ─────────────────────

  public void markSent() {
    this.status = Status.SENT;
    this.processedAt = Instant.now();
  }

  public void markFailed() {
    this.status = Status.FAILED;
    this.retryCount++;
    this.processedAt = Instant.now();
  }

  // ── Getters ───────────────────────────────────────────────────────────────

  public Long getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getEventType() {
    return eventType;
  }

  public String getPayload() {
    return payload;
  }

  public Status getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getProcessedAt() {
    return processedAt;
  }

  public int getRetryCount() {
    return retryCount;
  }
}
