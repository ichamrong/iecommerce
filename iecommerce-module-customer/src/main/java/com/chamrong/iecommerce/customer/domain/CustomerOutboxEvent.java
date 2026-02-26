package com.chamrong.iecommerce.customer.domain;

import com.chamrong.iecommerce.common.outbox.BaseOutboxEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "customer_outbox_event")
public class CustomerOutboxEvent extends BaseOutboxEvent {

  public static CustomerOutboxEvent pending(String tenantId, String eventType, String payload) {
    CustomerOutboxEvent e = new CustomerOutboxEvent();
    e.setTenantId(tenantId);
    e.setEventType(eventType);
    e.setPayload(payload);
    e.setStatus(Status.PENDING);
    e.setCreatedAt(Instant.now());
    return e;
  }
}
