package com.chamrong.iecommerce.sale.infrastructure.client;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.sale.domain.ports.InventoryPort;
import com.chamrong.iecommerce.sale.domain.ports.OrderItemInfo;
import com.chamrong.iecommerce.sale.domain.ports.OrderPort;
import com.chamrong.iecommerce.sale.domain.ports.PaymentPort;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Infrastructure clients for cross-module communication. */
@Slf4j
@Component
public class InfrastructureClientAdapter implements InventoryPort, PaymentPort, OrderPort {

  @Override
  public void reserveStock(String productId, BigDecimal quantity, String tenantId) {
    log.info(
        "Client [Inventory]: Reserving {} for product {} in tenant {}",
        quantity,
        productId,
        tenantId);
  }

  @Override
  public void releaseStock(String productId, BigDecimal quantity, String tenantId) {
    log.info(
        "Client [Inventory]: Releasing {} for product {} in tenant {}",
        quantity,
        productId,
        tenantId);
  }

  @Override
  public void restock(String productId, BigDecimal quantity, String tenantId, String reason) {
    log.info(
        "Client [Inventory]: Restocking {} for product {} (Reason: {}) in tenant {}",
        quantity,
        productId,
        reason,
        tenantId);
  }

  @Override
  public void initiatePayment(String tenantId, Money amount, String correlationId) {
    log.info(
        "Client [Payment]: Initiating payment for correlation {} (Amount: {}) in tenant {}",
        correlationId,
        amount,
        tenantId);
  }

  @Override
  public void refund(Long orderId, Money amount, String tenantId, String reason) {
    log.info(
        "Client [Payment]: Executing refund for order {} (Amount: {}) in tenant {}",
        orderId,
        amount,
        tenantId);
  }

  @Override
  public void createSalesOrder(
      String tenantId, String customerId, Long quotationId, Money totalAmount) {
    log.info(
        "Client [Order]: Creating sales order for quotation {} in tenant {}",
        quotationId,
        tenantId);
  }

  @Override
  public boolean exists(Long orderId, String tenantId) {
    log.info("Client [Order]: Checking existence for order {} in tenant {}", orderId, tenantId);
    return true;
  }

  @Override
  public Optional<OrderItemInfo> getOrderItem(Long orderLineId, String tenantId) {
    log.info("Client [Order]: Fetching item {} in tenant {}", orderLineId, tenantId);
    return Optional.of(
        new OrderItemInfo(orderLineId, 1L, "PROD-1", BigDecimal.TEN, Money.zero("USD")));
  }
}
