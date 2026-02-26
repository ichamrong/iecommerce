package com.chamrong.iecommerce.inventory.application;

import com.chamrong.iecommerce.inventory.InventoryApi;
import com.chamrong.iecommerce.inventory.application.dto.AdjustStockRequest;
import com.chamrong.iecommerce.inventory.application.dto.StockLevelResponse;
import com.chamrong.iecommerce.inventory.application.dto.WarehouseRequest;
import com.chamrong.iecommerce.inventory.application.dto.WarehouseResponse;
import com.chamrong.iecommerce.inventory.domain.InventoryOutboxEvent;
import com.chamrong.iecommerce.inventory.domain.InventoryOutboxRepository;
import com.chamrong.iecommerce.inventory.domain.OutOfStockException;
import com.chamrong.iecommerce.inventory.domain.StockLevel;
import com.chamrong.iecommerce.inventory.domain.StockLevelRepository;
import com.chamrong.iecommerce.inventory.domain.StockMovement;
import com.chamrong.iecommerce.inventory.domain.StockMovementRepository;
import com.chamrong.iecommerce.inventory.domain.Warehouse;
import com.chamrong.iecommerce.inventory.domain.WarehouseRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService implements InventoryApi {

  private final StockLevelRepository stockLevelRepository;
  private final WarehouseRepository warehouseRepository;
  private final StockMovementRepository stockMovementRepository;
  private final InventoryOutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  public void saveOutbox(String tenantId, String eventType, Object event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      outboxRepository.save(InventoryOutboxEvent.pending(tenantId, eventType, payload));
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize inventory outbox event type={}", eventType, e);
      throw new IllegalStateException("Cannot serialize event for outbox", e);
    }
  }

  @Override
  @Transactional
  public void reserveStock(String tenantId, Long productId, int quantity) {
    List<StockLevel> levels = stockLevelRepository.findForUpdateByProductId(productId);
    StockLevel levelWithStock =
        levels.stream()
            .filter(l -> l.getAvailableQuantity() >= quantity)
            .findFirst()
            .orElseThrow(() -> new OutOfStockException(productId));

    levelWithStock.reserve(quantity);
    stockLevelRepository.save(levelWithStock);
    log.info(
        "Reserved {} units of product {} in warehouse {}",
        quantity,
        productId,
        levelWithStock.getWarehouseId());
  }

  @Override
  @Transactional
  public void releaseStock(String tenantId, Long productId, int quantity) {
    List<StockLevel> levels = stockLevelRepository.findForUpdateByProductId(productId);
    for (StockLevel level : levels) {
      if (level.getReservedQuantity() >= quantity) {
        level.release(quantity);
        stockLevelRepository.save(level);
        log.info(
            "Released {} units of product {} from warehouse {}",
            quantity,
            productId,
            level.getWarehouseId());
        return;
      }
    }
  }

  @Override
  @Transactional
  public void deductStock(String tenantId, Long productId, int quantity) {
    List<StockLevel> levels = stockLevelRepository.findForUpdateByProductId(productId);
    for (StockLevel level : levels) {
      if (level.getReservedQuantity() >= quantity) {
        level.deduct(quantity);
        stockLevelRepository.save(level);

        // Record movement
        StockMovement mv = new StockMovement();
        mv.setTenantId(tenantId);
        mv.setProductId(productId);
        mv.setWarehouseId(level.getWarehouseId());
        mv.setQuantity(-quantity);
        mv.setReason(StockMovement.MovementReason.SALE);
        mv.setComment("Order shipped");
        stockMovementRepository.save(mv);

        log.info(
            "Deducted {} units of product {} from warehouse {}",
            quantity,
            productId,
            level.getWarehouseId());
        return;
      }
    }
  }

  @Override
  @Transactional
  public void deductPosSaleStock(String tenantId, Long productId, int quantity, Long terminalId) {
    List<StockLevel> levels = stockLevelRepository.findForUpdateByProductId(productId);
    StockLevel levelWithStock =
        levels.stream()
            .filter(l -> l.getAvailableQuantity() >= quantity)
            .findFirst()
            .orElseThrow(
                () ->
                    new OutOfStockException(
                        "Not enough available stock for POS sale of product: " + productId));

    levelWithStock.deductInstantly(quantity);
    stockLevelRepository.save(levelWithStock);

    StockMovement mv = new StockMovement();
    mv.setTenantId(tenantId);
    mv.setProductId(productId);
    mv.setWarehouseId(levelWithStock.getWarehouseId());
    mv.setQuantity(-quantity);
    mv.setReason(StockMovement.MovementReason.SALE);
    mv.setComment("Instant POS sale at terminal " + terminalId);
    stockMovementRepository.save(mv);

    log.info(
        "Deducted {} units instantly for POS sale of product {} from warehouse {}",
        quantity,
        productId,
        levelWithStock.getWarehouseId());
  }

  // ── Warehouses ─────────────────────────────────────────────────────────────

  @Transactional
  public WarehouseResponse createWarehouse(String tenantId, WarehouseRequest req) {
    Warehouse w = new Warehouse();
    w.setTenantId(tenantId);
    w.setName(req.name());
    w.setLocation(req.location());
    return toWarehouseResponse(warehouseRepository.save(w));
  }

  @Transactional(readOnly = true)
  public List<WarehouseResponse> listWarehouses(String tenantId) {
    return warehouseRepository.findByTenantId(tenantId).stream()
        .map(this::toWarehouseResponse)
        .toList();
  }

  // ── Stock adjustments ──────────────────────────────────────────────────────

  @Transactional
  public StockLevelResponse adjustStock(String tenantId, AdjustStockRequest req) {
    StockLevel level =
        stockLevelRepository
            .findByProductIdAndWarehouseId(req.productId(), req.warehouseId())
            .orElseGet(
                () -> {
                  StockLevel sl = new StockLevel();
                  sl.setTenantId(tenantId);
                  sl.setProductId(req.productId());
                  sl.setWarehouseId(req.warehouseId());
                  sl.setQuantity(0);
                  return sl;
                });

    int newQty = level.getQuantity() + req.delta();
    if (newQty < 0) {
      throw new IllegalArgumentException(
          "Stock adjustment would result in negative quantity: " + newQty);
    }
    level.setQuantity(newQty);
    stockLevelRepository.save(level);

    // Record movement
    StockMovement mv = new StockMovement();
    mv.setTenantId(tenantId);
    mv.setProductId(req.productId());
    mv.setWarehouseId(req.warehouseId());
    mv.setQuantity(req.delta());
    mv.setReason(req.reason());
    mv.setComment(req.comment());
    stockMovementRepository.save(mv);

    log.info(
        "Stock adjusted productId={} warehouseId={} delta={} newQty={}",
        req.productId(),
        req.warehouseId(),
        req.delta(),
        newQty);

    return toStockResponse(level);
  }

  @Transactional(readOnly = true)
  public List<StockLevelResponse> getProductStock(Long productId) {
    return stockLevelRepository.findByProductId(productId).stream()
        .map(this::toStockResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<StockLevelResponse> getWarehouseStock(Long warehouseId) {
    return stockLevelRepository.findByWarehouseId(warehouseId).stream()
        .map(this::toStockResponse)
        .toList();
  }

  @Transactional
  public StockLevelResponse setStock(String tenantId, Long productId, Long warehouseId, int qty) {
    int current =
        stockLevelRepository
            .findByProductIdAndWarehouseId(productId, warehouseId)
            .map(StockLevel::getQuantity)
            .orElse(0);
    int delta = qty - current;
    return adjustStock(
        tenantId,
        new AdjustStockRequest(
            productId,
            warehouseId,
            delta,
            StockMovement.MovementReason.CORRECTION,
            "Manual override to " + qty));
  }

  // ── Low-stock check ────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public List<StockLevelResponse> getLowStock(String tenantId, int threshold) {
    return stockLevelRepository.findByTenantIdAndQuantityLessThanEqual(tenantId, threshold).stream()
        .map(this::toStockResponse)
        .toList();
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private WarehouseResponse toWarehouseResponse(Warehouse w) {
    return new WarehouseResponse(w.getId(), w.getName(), w.getLocation());
  }

  private StockLevelResponse toStockResponse(StockLevel sl) {
    return new StockLevelResponse(
        sl.getId(), sl.getProductId(), sl.getWarehouseId(), sl.getQuantity());
  }
}
