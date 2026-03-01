package com.chamrong.iecommerce.invoice.application;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.invoice.application.dto.CreateInvoiceRequest;
import com.chamrong.iecommerce.invoice.application.dto.InvoiceResponse;
import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceLine;
import com.chamrong.iecommerce.invoice.domain.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Legacy invoice service — retained for backwards compatibility with older API endpoints.
 *
 * <p>New features should use {@link InvoiceApplicationService} instead, which implements the full
 * DDD/hexagonal model with Ed25519 signing, outbox events, and cursor pagination.
 *
 * @deprecated Use {@link InvoiceApplicationService} for new work.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Deprecated(since = "v20", forRemoval = false)
public class InvoiceService {

  private final InvoiceRepository invoiceRepository;

  @Transactional
  public InvoiceResponse create(String tenantId, CreateInvoiceRequest req) {
    if (req.idempotencyKey() != null && !req.idempotencyKey().isBlank()) {
      Optional<Invoice> existing = invoiceRepository.findByIdempotencyKey(req.idempotencyKey());
      if (existing.isPresent()) {
        log.info(
            "Duplicate invoice request detected for key={}, returning existing invoice",
            req.idempotencyKey());
        return toResponse(existing.get());
      }
    }

    // Use the new factory method — no direct field injection
    Invoice invoice =
        Invoice.createDraft(
            tenantId,
            req.orderId(),
            null, // customerId not in legacy request
            req.currency(),
            null, // dueDate not in legacy request
            null, // sellerSnapshot not in legacy request
            null); // buyerSnapshot not in legacy request

    // Populate lines via domain void method
    req.lines()
        .forEach(
            l -> {
              InvoiceLine line =
                  InvoiceLine.of(
                      null,
                      l.productName(),
                      null,
                      l.quantity(),
                      new Money(l.unitPriceAmount(), req.currency()),
                      java.math.BigDecimal.ZERO, // taxRate defaults to zero for legacy requests
                      0);
              invoice.addLine(line);
            });

    log.info("Invoice DRAFT created for orderId={}", req.orderId());
    return toResponse(invoiceRepository.save(invoice));
  }

  @Transactional(readOnly = true)
  public Optional<InvoiceResponse> findById(Long id) {
    return invoiceRepository.findById(id).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public List<InvoiceResponse> findByOrderId(Long orderId) {
    return invoiceRepository.findByOrderId(orderId).stream().map(this::toResponse).toList();
  }

  private Invoice require(Long id) {
    return invoiceRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + id));
  }

  private InvoiceResponse toResponse(Invoice inv) {
    return new InvoiceResponse(
        inv.getId(),
        inv.getInvoiceNumber(),
        inv.getOrderId(),
        inv.getIssueDate(),
        inv.getStatus().name(),
        inv.getTotal() != null ? new Money(inv.getTotal(), inv.getCurrency()) : null,
        inv.getCreatedAt());
  }
}
