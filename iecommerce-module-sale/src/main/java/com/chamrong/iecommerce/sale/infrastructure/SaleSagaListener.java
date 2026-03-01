package com.chamrong.iecommerce.sale.infrastructure;

import com.chamrong.iecommerce.common.event.SaleSessionCompletedEvent;
import com.chamrong.iecommerce.invoice.application.InvoiceApplicationService;
import com.chamrong.iecommerce.invoice.application.command.CreateInvoiceDraftCommand;
import com.chamrong.iecommerce.invoice.application.command.IssueInvoiceCommand;
import com.chamrong.iecommerce.invoice.application.dto.InvoiceDetailResponse;
import com.chamrong.iecommerce.sale.domain.repository.SaleSessionRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saga Orchestrator for Sale flows. Handles transitions between Sale, Invoice, and Payment modules.
 *
 * <p>Acts as a background SYSTEM actor — no JWT available, so actor is hardcoded to "SYSTEM".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaleSagaListener {

  private static final String SYSTEM_ACTOR = "SYSTEM";

  private final InvoiceApplicationService invoiceService;
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
      // ── Step 1: create DRAFT invoice ─────────────────────────────────────
      // Parse customerId: SaleSessionCompletedEvent carries it as String; command needs Long
      Long customerId = null;
      if (event.customerId() != null) {
        try {
          customerId = Long.parseLong(event.customerId());
        } catch (NumberFormatException ex) {
          log.warn(
              "Saga [Sale]: customerId '{}' is not a valid Long — treating as null",
              event.customerId());
        }
      }

      CreateInvoiceDraftCommand draftCmd =
          new CreateInvoiceDraftCommand(
              event.tenantId(),
              SYSTEM_ACTOR,
              event.orderId(),
              customerId,
              event.currency() != null ? event.currency() : "USD",
              LocalDate.now().plusDays(30), // default net-30 due date
              null, // sellerSnapshot — populated by InvoiceApplicationService from config
              null, // buyerSnapshot — populated later when customer data is resolved
              List.of()); // lines added later via the line-management flow

      InvoiceDetailResponse draft = invoiceService.createDraft(draftCmd);
      log.info(
          "Saga [Sale]: Invoice {} (id={}) created for session {}. Proceeding to issue.",
          draft.invoiceNumber(),
          draft.id(),
          event.sessionId());

      // ── Step 2: auto-issue for POS sales (DRAFT → ISSUED + Ed25519 sign) ─
      IssueInvoiceCommand issueCmd =
          new IssueInvoiceCommand(
              event.tenantId(),
              SYSTEM_ACTOR,
              draft.id(),
              null, // sellerSnapshot — keep from draft
              null); // buyerSnapshot — keep from draft

      invoiceService.issueInvoice(issueCmd);
      log.info(
          "Saga [Sale]: Invoice id={} issued and signed for session {}.",
          draft.id(),
          event.sessionId());

    } catch (Exception e) {
      log.error(
          "Saga [Sale]: Failed to orchestrate invoice for session {}: {}",
          event.sessionId(),
          e.getMessage());

      // Compensation: mark session INVOICE_FAILED so staff can retry
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
