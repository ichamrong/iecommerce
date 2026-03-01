package com.chamrong.iecommerce.sale.application.usecase;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.sale.application.command.CreateReturnCommand;
import com.chamrong.iecommerce.sale.application.dto.SaleReturnResponse;
import com.chamrong.iecommerce.sale.domain.exception.SaleDomainException;
import com.chamrong.iecommerce.sale.domain.model.SaleReturn;
import com.chamrong.iecommerce.sale.domain.ports.OrderItemInfo;
import com.chamrong.iecommerce.sale.domain.ports.OrderPort;
import com.chamrong.iecommerce.sale.domain.repository.SaleReturnRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnUseCase {

  private final SaleReturnRepositoryPort repository;
  private final OrderPort orderPort;

  @Transactional
  public SaleReturnResponse createReturn(CreateReturnCommand command) {
    log.info(
        "Saga [Return]: Processing return for order {} in tenant {}",
        command.originalOrderId(),
        command.tenantId());

    // Idempotency check
    Optional<SaleReturn> existing =
        repository.findByReturnKey(command.tenantId(), command.returnKey());
    if (existing.isPresent()) {
      log.info(
          "Saga [Return]: Return with key {} already exists, returning existing",
          command.returnKey());
      return toResponse(existing.get());
    }

    if (!orderPort.exists(command.originalOrderId(), command.tenantId())) {
      throw new SaleDomainException("Original order not found or context mismatch");
    }

    SaleReturn saleReturn =
        new SaleReturn(
            command.tenantId(),
            command.originalOrderId(),
            command.returnKey(),
            command.reason(),
            command.currency());

    for (CreateReturnCommand.ReturnItemLine line : command.items()) {
      OrderItemInfo originalItem =
          orderPort
              .getOrderItem(line.originalLineId(), command.tenantId())
              .orElseThrow(
                  () ->
                      new SaleDomainException(
                          "Original order line not found: " + line.originalLineId()));

      // Anti-fraud: Cannot return more than purchased
      if (line.quantity().compareTo(originalItem.getQuantity()) > 0) {
        throw new SaleDomainException(
            "Cannot return more than originally purchased for line " + line.originalLineId());
      }

      saleReturn.addItem(
          line.originalLineId(),
          line.quantity(),
          new Money(line.refundPrice(), command.currency()));
    }

    return toResponse(repository.save(saleReturn));
  }

  @Transactional
  public SaleReturnResponse approveReturn(Long id, String tenantId, String approverId) {
    SaleReturn saleReturn =
        repository
            .findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new EntityNotFoundException("Return not found"));

    saleReturn.approve(approverId);
    return toResponse(repository.save(saleReturn));
  }

  @Transactional
  public SaleReturnResponse completeReturn(Long id, String tenantId) {
    SaleReturn saleReturn =
        repository
            .findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new EntityNotFoundException("Return not found"));

    saleReturn.complete();
    return toResponse(repository.save(saleReturn));
  }

  private SaleReturnResponse toResponse(SaleReturn r) {
    return new SaleReturnResponse(
        r.getId(),
        r.getOriginalOrderId(),
        r.getReturnKey(),
        r.getStatus().name(),
        r.getReason(),
        r.getTotalRefundAmount(),
        r.getRequestedAt(),
        r.getCompletedAt(),
        r.getItems().stream()
            .map(
                i ->
                    new SaleReturnResponse.ReturnItemResponse(
                        i.getId(),
                        i.getOriginalLineId(),
                        i.getQuantity(),
                        i.getRefundPrice(),
                        i.getTotalRefundAmount()))
            .toList());
  }
}
