package com.chamrong.iecommerce.payment.domain;

import com.chamrong.iecommerce.common.outbox.BaseOutboxEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_outbox_event")
@Getter
@Setter
@NoArgsConstructor
public class PaymentOutboxEvent extends BaseOutboxEvent {

  public static PaymentOutboxEvent pending(String tenantId, String eventType, String payload) {
    PaymentOutboxEvent e = new PaymentOutboxEvent();
    e.setTenantId(tenantId);
    e.setEventType(eventType);
    e.setPayload(payload);
    e.setStatus(Status.PENDING);
    e.setCreatedAt(Instant.now());
    return e;
  }
}
