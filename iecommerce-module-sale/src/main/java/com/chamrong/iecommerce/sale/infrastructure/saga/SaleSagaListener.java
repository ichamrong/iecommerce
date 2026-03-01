package com.chamrong.iecommerce.sale.infrastructure.saga;

import com.chamrong.iecommerce.invoice.application.InvoiceApplicationService;
import com.chamrong.iecommerce.sale.domain.event.SaleSessionClosedEvent;
import com.chamrong.iecommerce.sale.domain.repository.SaleSessionRepositoryPort;
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

  private static final String SYSTEM_ACTOR = "SYSTEM";

  private final InvoiceApplicationService invoiceService;
  private final SaleSessionRepositoryPort sessionRepository;

  @EventListener
  @Transactional
  public void onSaleSessionClosed(SaleSessionClosedEvent event) {
    log.info(
        "Saga [Sale]: Session {} closed in tenant {}. Initiating Invoicing.",
        event.sessionId(),
        event.tenantId());

    try {
      // Logic for session wrap-up invoicing
      log.info(
          "Saga [Sale]: Invoicing logic for session {} triggered for amount {}.",
          event.sessionId(),
          event.actualAmount());

      // Placeholder for actual invoice creation if session is linked to an order
      // In POS, we might create a summary invoice or multiple individual ones.
    } catch (Exception e) {
      log.error(
          "Saga [Sale]: Failed to orchestrate invoice for session {}: {}",
          event.sessionId(),
          e.getMessage());
    }
  }
}
