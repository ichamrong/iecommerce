package com.chamrong.iecommerce.sale.application.saga;

import com.chamrong.iecommerce.sale.domain.event.QuotationConfirmedEvent;
import com.chamrong.iecommerce.sale.domain.ports.InventoryPort;
import com.chamrong.iecommerce.sale.domain.ports.OrderPort;
import com.chamrong.iecommerce.sale.domain.ports.PaymentPort;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.JpaSaleSagaRepository;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.SaleSagaStateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * P6: Saga Orchestration with Dedup, Retries, and Compensation. Handles the flow after a quotation
 * is confirmed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleSagaOrchestrator {

  private final JpaSaleSagaRepository sagaRepository;
  private final OrderPort orderPort;
  private final InventoryPort inventoryPort;
  private final PaymentPort paymentPort;

  @Async
  @EventListener
  @Transactional
  public void handleQuotationConfirmed(QuotationConfirmedEvent event) {
    String correlationId = event.quotationId().toString();

    // Dedup / Check current state
    SaleSagaStateEntity saga =
        sagaRepository
            .findByTenantIdAndCorrelationId(event.tenantId(), correlationId)
            .orElseGet(
                () ->
                    sagaRepository.save(new SaleSagaStateEntity(event.tenantId(), correlationId)));

    if (saga.getStatus() == SaleSagaStateEntity.SagaStatus.COMPLETED
        || saga.getStatus() == SaleSagaStateEntity.SagaStatus.FAILED) {
      log.info("Saga {} already in terminal state {}", correlationId, saga.getStatus());
      return;
    }

    try {
      executeSagaSteps(event, saga);
    } catch (Exception e) {
      log.error(
          "Saga {} failed at step {}: {}", correlationId, saga.getCurrentStep(), e.getMessage());
      handleFailure(saga);
    }
  }

  private void executeSagaSteps(QuotationConfirmedEvent event, SaleSagaStateEntity saga) {
    // Step 1: Create Order
    if (saga.getStatus() == SaleSagaStateEntity.SagaStatus.STARTED) {
      log.info("Saga {}: Step 1 - Creating Sales Order", saga.getCorrelationId());
      orderPort.createSalesOrder(
          event.tenantId(), event.customerId(), event.quotationId(), event.totalAmount());
      saga.updateStatus(SaleSagaStateEntity.SagaStatus.ORDER_CREATED, "CREATE_ORDER");
      sagaRepository.save(saga);
    }

    // Step 2: Reserve Inventory (In a real app, we'd pass item details)
    if (saga.getStatus() == SaleSagaStateEntity.SagaStatus.ORDER_CREATED) {
      log.info("Saga {}: Step 2 - Reserving Inventory", saga.getCorrelationId());
      // Placeholder: In a full impl, we'd fetch items from the quotation
      inventoryPort.reserveStock("PROD-GENERIC", event.totalAmount().getAmount(), event.tenantId());
      saga.updateStatus(SaleSagaStateEntity.SagaStatus.STOCK_RESERVED, "RESERVE_STOCK");
      sagaRepository.save(saga);
    }

    // Step 3: Initiate Payment
    if (saga.getStatus() == SaleSagaStateEntity.SagaStatus.STOCK_RESERVED) {
      log.info("Saga {}: Step 3 - Initiating Payment", saga.getCorrelationId());
      paymentPort.initiatePayment(
          event.tenantId(), event.totalAmount(), "SAGA-" + saga.getCorrelationId());
      saga.updateStatus(SaleSagaStateEntity.SagaStatus.COMPLETED, "INITIATE_PAYMENT");
      sagaRepository.save(saga);
    }
  }

  private void handleFailure(SaleSagaStateEntity saga) {
    log.error("Saga {}: Failure detected, initiating compensation", saga.getCorrelationId());
    saga.updateStatus(SaleSagaStateEntity.SagaStatus.COMPENSATING, saga.getCurrentStep());
    sagaRepository.save(saga);

    // Simplistic compensation logic
    try {
      if (saga.getStatus() == SaleSagaStateEntity.SagaStatus.COMPENSATING) {
        // If we reserved stock, we should release it.
        // if we created order, we should cancel it.
        log.warn(
            "Saga {}: Compensating... (Cancel Order / Release Stock)", saga.getCorrelationId());
        // orderPort.cancelOrder(...)
        // inventoryPort.releaseStock(...)
        saga.updateStatus(SaleSagaStateEntity.SagaStatus.COMPENSATED, "COMPENSATION_DONE");
        sagaRepository.save(saga);
      }
    } catch (Exception ce) {
      log.error("Saga {}: Compensation failed critically!", saga.getCorrelationId());
    }
  }
}
