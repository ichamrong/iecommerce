package com.chamrong.iecommerce.invoice.infrastructure.persistence;

import com.chamrong.iecommerce.invoice.domain.port.InvoiceOutboxPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter: implements {@link InvoiceOutboxPort} by writing events to the {@code
 * invoice_outbox_event} table within the current transaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JpaInvoiceOutboxAdapter implements InvoiceOutboxPort {

  private final SpringDataInvoiceOutboxRepository outboxRepo;
  private final ObjectMapper objectMapper;

  @Override
  public void publishIssued(String tenantId, Long invoiceId, String invoiceNumber, Long orderId) {
    String payload =
        toJson(
            new InvoiceOutboxRelay.InvoiceIssuedPayload(
                tenantId, invoiceId, invoiceNumber, orderId));
    outboxRepo.save(InvoiceOutboxEvent.pending(tenantId, "INVOICE_ISSUED", payload, invoiceId));
    log.debug("Enqueued INVOICE_ISSUED for invoiceId={}", invoiceId);
  }

  @Override
  public void publishVoided(String tenantId, Long invoiceId, String reason) {
    String payload =
        toJson(new InvoiceOutboxRelay.InvoiceVoidedPayload(tenantId, invoiceId, reason));
    outboxRepo.save(InvoiceOutboxEvent.pending(tenantId, "INVOICE_VOIDED", payload, invoiceId));
    log.debug("Enqueued INVOICE_VOIDED for invoiceId={}", invoiceId);
  }

  @Override
  public void publishPaid(String tenantId, Long invoiceId, String paymentReference) {
    String payload =
        toJson(new InvoiceOutboxRelay.InvoicePaidPayload(tenantId, invoiceId, paymentReference));
    outboxRepo.save(InvoiceOutboxEvent.pending(tenantId, "INVOICE_PAID", payload, invoiceId));
    log.debug("Enqueued INVOICE_PAID for invoiceId={}", invoiceId);
  }

  private String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize outbox payload", e);
    }
  }
}
