package com.chamrong.iecommerce.order.domain;

import com.chamrong.iecommerce.common.outbox.BaseOutboxEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "order_outbox_event")
public class OrderOutboxEvent extends BaseOutboxEvent {

  public static OrderOutboxEvent pending(String tenantId, String eventType, String payload) {
    OrderOutboxEvent e = new OrderOutboxEvent();
    e.setTenantId(tenantId);
    e.setEventType(eventType);
    e.setPayload(payload);
    e.setStatus(Status.PENDING);
    e.setCreatedAt(Instant.now());
    return e;
  }
}
