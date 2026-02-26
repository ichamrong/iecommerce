package com.chamrong.iecommerce.invoice.application;

import com.chamrong.iecommerce.invoice.application.dto.CreateInvoiceRequest;
import com.chamrong.iecommerce.invoice.application.dto.InvoiceResponse;
import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceLine;
import com.chamrong.iecommerce.invoice.domain.InvoiceRepository;
import com.chamrong.iecommerce.invoice.domain.InvoiceStatus;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

  private final InvoiceRepository invoiceRepository;

  @Transactional
  public InvoiceResponse create(String tenantId, CreateInvoiceRequest req) {
    Invoice invoice = new Invoice();
    invoice.setTenantId(tenantId);
    invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    invoice.setOrderId(req.orderId());
    invoice.setInvoiceDate(Instant.now());
    invoice.setStatus(InvoiceStatus.DRAFT);

    // Populate lines
    List<InvoiceLine> lines =
        req.lines().stream()
            .map(
                l -> {
                  InvoiceLine line = new InvoiceLine();
                  line.setProductName(l.productName());
                  line.setQuantity(l.quantity());
                  line.setUnitPrice(new Money(l.unitPriceAmount(), req.currency()));
                  return line;
                })
            .toList();
    invoice.setLines(lines);

    // Compute total
    BigDecimal total =
        lines.stream()
            .map(l -> l.getUnitPrice().getAmount().multiply(BigDecimal.valueOf(l.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    invoice.setTotalAmount(new Money(total, req.currency()));

    log.info("Invoice created orderId={} total={}", req.orderId(), total);
    return toResponse(invoiceRepository.save(invoice));
  }

  @Transactional
  public InvoiceResponse issue(Long id) {
    Invoice inv = require(id);
    inv.issue();
    return toResponse(invoiceRepository.save(inv));
  }

  @Transactional
  public InvoiceResponse markPaid(Long id) {
    Invoice inv = require(id);
    inv.markPaid();
    return toResponse(invoiceRepository.save(inv));
  }

  @Transactional
  public InvoiceResponse voidInvoice(Long id) {
    Invoice inv = require(id);
    inv.void_();
    return toResponse(invoiceRepository.save(inv));
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
        inv.getInvoiceDate(),
        inv.getStatus().name(),
        inv.getTotalAmount(),
        inv.getCreatedAt());
  }
}
