package com.chamrong.iecommerce.sale.application.usecase;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.exception.RateLimitException;
import com.chamrong.iecommerce.sale.application.command.CreateQuotationCommand;
import com.chamrong.iecommerce.sale.application.dto.QuotationResponse;
import com.chamrong.iecommerce.sale.domain.model.Quotation;
import com.chamrong.iecommerce.sale.domain.ports.QuotationRepositoryPort;
import com.chamrong.iecommerce.sale.domain.service.AuditService;
import com.chamrong.iecommerce.sale.domain.service.IdempotencyService;
import com.chamrong.iecommerce.sale.domain.service.LogMasker;
import com.chamrong.iecommerce.sale.domain.service.RateLimiter;
import com.chamrong.iecommerce.sale.infrastructure.outbox.OutboxPublisher;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationUseCase {

  private final QuotationRepositoryPort repository;
  private final OutboxPublisher outboxPublisher;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;
  private final RateLimiter rateLimiter;

  @Transactional
  public QuotationResponse createQuotation(CreateQuotationCommand command, String idempotencyKey) {
    if (!rateLimiter.tryConsume(command.tenantId() + ":quotation:create")) {
      throw new RateLimitException("Too many quotation requests");
    }

    return (QuotationResponse)
        idempotencyService.execute(
            command.tenantId(),
            idempotencyKey,
            "createQuotation",
            command.toString(),
            () -> {
              Quotation quotation =
                  new Quotation(
                      command.tenantId(),
                      command.customerId(),
                      command.currency(),
                      command.expiryDate());
              for (CreateQuotationCommand.QuotationItemLine line : command.items()) {
                quotation.addItem(
                    line.productId(),
                    line.quantity(),
                    new Money(line.unitPrice(), command.currency()));
              }
              Quotation saved = repository.save(quotation);

              auditService.log(
                  command.tenantId(),
                  "SYSTEM",
                  "CREATE",
                  "Quotation",
                  saved.getId().toString(),
                  "N/A",
                  null,
                  LogMasker.mask(saved));

              return toResponse(saved);
            });
  }

  @Transactional
  public QuotationResponse confirmQuotation(Long id, String tenantId, String idempotencyKey) {
    return (QuotationResponse)
        idempotencyService.execute(
            tenantId,
            idempotencyKey,
            "confirmQuotation",
            id.toString(),
            () -> {
              Quotation quotation =
                  repository
                      .findByIdAndTenantId(id, tenantId)
                      .orElseThrow(() -> new EntityNotFoundException("Quotation not found: " + id));

              String beforeState = quotation.toString();
              quotation.confirm();
              Quotation saved = repository.save(quotation);

              auditService.log(
                  tenantId,
                  "SYSTEM",
                  "CONFIRM",
                  "Quotation",
                  saved.getId().toString(),
                  "N/A",
                  beforeState,
                  saved.toString());

              outboxPublisher.publish(
                  tenantId,
                  new com.chamrong.iecommerce.sale.domain.event.QuotationConfirmedEvent(
                      saved.getId(),
                      tenantId,
                      saved.getCustomerId(),
                      saved.getTotalAmount(),
                      java.time.Instant.now()),
                  saved.getId());

              return toResponse(saved);
            });
  }

  @Transactional
  public QuotationResponse cancelQuotation(Long id, String tenantId) {
    Quotation quotation =
        repository
            .findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new EntityNotFoundException("Quotation not found: " + id));

    quotation.cancel();
    return toResponse(repository.save(quotation));
  }

  private QuotationResponse toResponse(Quotation q) {
    return new QuotationResponse(
        q.getId(),
        q.getCustomerId(),
        q.getExpiryDate(),
        q.getTotalAmount(),
        q.getStatus().name(),
        q.getItems().stream()
            .map(
                i ->
                    new QuotationResponse.QuotationItemResponse(
                        i.getId(),
                        i.getProductId(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getTotalPrice()))
            .toList());
  }
}
