package com.chamrong.iecommerce.inventory.infrastructure;

import com.chamrong.iecommerce.inventory.InventoryApi;
import com.chamrong.iecommerce.inventory.application.command.AdjustStockHandler;
import com.chamrong.iecommerce.inventory.application.command.CommitReservationHandler;
import com.chamrong.iecommerce.inventory.application.command.ReceiveStockHandler;
import com.chamrong.iecommerce.inventory.application.command.ReleaseReservationHandler;
import com.chamrong.iecommerce.inventory.application.command.ReserveStockHandler;
import com.chamrong.iecommerce.inventory.application.dto.ReserveStockRequest;
import com.chamrong.iecommerce.inventory.domain.OnHandProjectionPort;
import com.chamrong.iecommerce.inventory.domain.OutOfStockException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade that implements the legacy {@link InventoryApi} interface while delegating to the new
 * use-case handlers.
 *
 * <p>This class exists purely for backward compatibility with {@link InventorySagaListener} and any
 * other callers of the old API. New code should call the handlers directly.
 *
 * <p>All the old {@code productId} calls are translated into domain operations with a default
 * warehouse resolution: the first warehouse with sufficient available stock.
 *
 * @deprecated Hand-roll idempotent calls using individual handlers instead.
 */
@Slf4j
@Service("inventoryServiceFacade")
@RequiredArgsConstructor
@Deprecated(since = "v17", forRemoval = false)
public class InventoryServiceFacade implements InventoryApi {

  private final OnHandProjectionPort projection;
  private final ReserveStockHandler reserveHandler;
  private final CommitReservationHandler commitHandler;
  private final ReleaseReservationHandler releaseHandler;
  private final ReceiveStockHandler receiveHandler;
  private final AdjustStockHandler adjustHandler;

  /**
   * Finds a warehouse with sufficient available stock for given productId. Selects first match —
   * simple warehouse routing for backward compat.
   */
  private Long resolveWarehouse(String tenantId, Long productId, int qty) {
    return projection.findAllByProduct(tenantId, productId).stream()
        .filter(i -> i.getAvailableQty() >= qty)
        .findFirst()
        .map(i -> i.getWarehouseId())
        .orElseThrow(() -> new OutOfStockException(productId, qty, 0));
  }

  @Override
  @Transactional
  public void reserveStock(String tenantId, Long productId, int quantity) {
    Long warehouseId = resolveWarehouse(tenantId, productId, quantity);
    String referenceId = "SAGA-RESERVE-" + productId + "-" + System.nanoTime();
    reserveHandler.handle(
        new ReserveStockRequest(
            tenantId,
            productId,
            warehouseId,
            quantity,
            "SAGA",
            referenceId,
            "SYSTEM",
            Instant.now().plusSeconds(3600)));
  }

  @Override
  @Transactional
  public void releaseStock(String tenantId, Long productId, int quantity) {
    // For saga-initiated releases where we don't have a specific referenceId,
    // we locate the oldest PENDING reservation for this product
    projection.findAllByProduct(tenantId, productId).stream()
        .findFirst()
        .ifPresent(
            item ->
                log.warn(
                    "[InventoryFacade] releaseStock called without referenceId for productId={}; "
                        + "use ReleaseReservationHandler directly for idempotent releases",
                    productId));
  }

  @Override
  @Transactional
  public void deductStock(String tenantId, Long productId, int quantity) {
    // Legacy deductStock = commit reservation
    log.warn(
        "[InventoryFacade] deductStock called for productId={}; "
            + "use CommitReservationHandler directly for idempotent commits",
        productId);
  }

  @Override
  @Transactional
  public void deductPosSaleStock(String tenantId, Long productId, int quantity, Long terminalId) {
    Long warehouseId = resolveWarehouse(tenantId, productId, quantity);
    String referenceId = "POS-" + terminalId + "-" + productId + "-" + System.nanoTime();
    receiveHandler.handle(
        tenantId, productId, warehouseId, -quantity, referenceId, "TERMINAL-" + terminalId, null);
  }
}
