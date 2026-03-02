package com.chamrong.iecommerce.review.domain;

import com.chamrong.iecommerce.common.outbox.BaseOutboxEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Outbox event entity for review-related domain events.
 *
 * <p>Events are written to this table within the same transaction as the review aggregate change
 * and relayed asynchronously by the outbox scheduler.
 */
@Entity
@Table(name = "review_outbox_event")
@Getter
@Setter
@NoArgsConstructor
public class ReviewOutboxEvent extends BaseOutboxEvent {

  /**
   * Factory for a new pending outbox event.
   *
   * @param tenantId tenant identifier
   * @param eventType logical event type name
   * @param payload JSON payload
   * @return newly created pending outbox event
   */
  public static ReviewOutboxEvent pending(String tenantId, String eventType, String payload) {
    ReviewOutboxEvent e = new ReviewOutboxEvent();
    e.setTenantId(tenantId);
    e.setEventType(eventType);
    e.setPayload(payload);
    e.setStatus(Status.PENDING);
    e.setCreatedAt(Instant.now());
    return e;
  }
}
