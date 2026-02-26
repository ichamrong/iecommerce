package com.chamrong.iecommerce.sale.infrastructure;

import com.chamrong.iecommerce.common.event.SaleSessionCompletedEvent;
import com.chamrong.iecommerce.invoice.application.InvoiceService;
import com.chamrong.iecommerce.invoice.application.dto.CreateInvoiceRequest;
import com.chamrong.iecommerce.sale.domain.repository.SaleSessionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saga Orchestrator for Sale flows. Handles transitions between Sale, Invoice, and Payment modules.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaleSagaListener {

  private final InvoiceService invoiceService;
  private final SaleSessionRepository sessionRepository;

  @EventListener
  @Transactional
  public void onSaleSessionCompleted(SaleSessionCompletedEvent event) {
    log.info(
        "Saga [Sale]: Session {} completed for customer {}. Initiating Invoicing.",
        event.sessionId(),
        event.customerId());

    if (event.orderId() == null) {
      log.warn(
          "Saga [Sale]: No orderId found for session {}. Skipping invoice creation.",
          event.sessionId());
      return;
    }

    try {
      // Initiate Invoice creation for the order linked to the Sale Session
      CreateInvoiceRequest request =
          new CreateInvoiceRequest(
              event.orderId(),
              event.currency(),
              List.of(), // In a real scenario, this would be populated from the Order items
              null // idempotencyKey
              );

      var invoice = invoiceService.create(event.tenantId(), request);
      log.info(
          "Saga [Sale]: Invoice {} created for session {}. Proceeding to issue.",
          invoice.invoiceNumber(),
          event.sessionId());

      // Auto-issue the invoice for POS sales to prepare for payment
      invoiceService.issue(invoice.id());

    } catch (Exception e) {
      log.error(
          "Saga [Sale]: Failed to orchestrate invoice for session {}: {}",
          event.sessionId(),
          e.getMessage());

      // Compensation logic: Mark session as INVOICE_FAILED to notify staff
      sessionRepository
          .findById(event.sessionId())
          .ifPresent(
              s -> {
                s.setStatus(
                    com.chamrong.iecommerce.sale.domain.SaleSession.SessionStatus.INVOICE_FAILED);
                sessionRepository.save(s);
                log.warn(
                    "Saga [Sale]: Session {} marked as INVOICE_FAILED via compensation.",
                    event.sessionId());
              });
    }
  }
}
