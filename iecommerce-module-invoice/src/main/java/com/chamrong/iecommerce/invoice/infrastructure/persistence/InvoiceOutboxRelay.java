package com.chamrong.iecommerce.invoice.infrastructure.persistence;

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
 * Scheduled relay that polls the {@code invoice_outbox_event} table and dispatches PENDING events.
 *
 * <p>Multi-instance safe via {@code SKIP LOCKED}: each pod claims a distinct batch.
 *
 * <p>Retries with exponential backoff + jitter on failure.
 *
 * <p>ASVS V14.5 — Event publishing is idempotent: downstream consumers must also be idempotent
 * (deduplicate on {@code invoiceId + eventType}).
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

  /**
   * Polls every 5 seconds. The SKIP LOCKED query prevents multiple instances from processing the
   * same row simultaneously.
   */
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

  // ── Internal payload records (for deserialization) ─────────────────────────

  public record InvoiceIssuedPayload(
      String tenantId, Long invoiceId, String invoiceNumber, Long orderId) {}

  public record InvoiceVoidedPayload(String tenantId, Long invoiceId, String reason) {}

  public record InvoicePaidPayload(String tenantId, Long invoiceId, String paymentReference) {}
}
