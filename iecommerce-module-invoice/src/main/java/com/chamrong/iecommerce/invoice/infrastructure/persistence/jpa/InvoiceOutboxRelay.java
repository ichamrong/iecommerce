package com.chamrong.iecommerce.invoice.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.common.EventDispatcher;
import com.chamrong.iecommerce.common.outbox.AbstractOutboxRelay;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled relay that polls the invoice_outbox_event table and dispatches PENDING events.
 * Multi-instance safe via SKIP LOCKED.
 */
@Slf4j
@Component
public class InvoiceOutboxRelay extends AbstractOutboxRelay<InvoiceOutboxEvent> {

  private static final int BATCH_SIZE = 50;
  private static final int BASE_BACKOFF_SECONDS = 5;

  private final SpringDataInvoiceOutboxRepository outboxRepo;

  public InvoiceOutboxRelay(
      EventDispatcher eventDispatcher,
      ObjectMapper objectMapper,
      SpringDataInvoiceOutboxRepository outboxRepo) {
    super(eventDispatcher, objectMapper);
    this.outboxRepo = outboxRepo;
  }

  @Transactional
  @Scheduled(fixedDelay = 5000)
  public void relay() {
    List<InvoiceOutboxEvent> pending = outboxRepo.claimPending(Instant.now(), BATCH_SIZE);
    if (!pending.isEmpty()) {
      log.debug("InvoiceOutboxRelay: processing {} events", pending.size());
      processPendingEvents(pending);
    }
  }

  @Override
  protected Class<?> getEventClass(String eventType) {
    return switch (eventType) {
      case "INVOICE_ISSUED" -> InvoiceIssuedPayload.class;
      case "INVOICE_VOIDED" -> InvoiceVoidedPayload.class;
      case "INVOICE_PAID" -> InvoicePaidPayload.class;
      default -> throw new IllegalArgumentException("Unknown invoice event type: " + eventType);
    };
  }

  @Override
  protected void saveEvent(InvoiceOutboxEvent event) {
    if (event.getStatus() == com.chamrong.iecommerce.common.outbox.BaseOutboxEvent.Status.FAILED) {
      event.applyBackoff(BASE_BACKOFF_SECONDS);
    }
    outboxRepo.save(event);
  }

  public record InvoiceIssuedPayload(
      String tenantId, Long invoiceId, String invoiceNumber, Long orderId) {}

  public record InvoiceVoidedPayload(String tenantId, Long invoiceId, String reason) {}

  public record InvoicePaidPayload(String tenantId, Long invoiceId, String paymentReference) {}
}
