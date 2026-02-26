package com.chamrong.iecommerce.inventory.api;

import com.chamrong.iecommerce.inventory.application.InventoryService;
import com.chamrong.iecommerce.inventory.application.dto.AdjustStockRequest;
import com.chamrong.iecommerce.inventory.application.dto.StockLevelResponse;
import com.chamrong.iecommerce.inventory.application.dto.WarehouseRequest;
import com.chamrong.iecommerce.inventory.application.dto.WarehouseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inventory management — warehouses and stock levels.
 *
 * <p>Base path: {@code /api/v1/inventory}
 */
@Tag(name = "Inventory", description = "Warehouse and stock level management")
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('inventory:read') or hasAuthority('inventory:manage')")
public class InventoryController {

  private final InventoryService inventoryService;

  // ── Warehouses ─────────────────────────────────────────────────────────────

  @Operation(summary = "List warehouses for a tenant")
  @GetMapping("/warehouses")
  public List<WarehouseResponse> listWarehouses(@RequestParam String tenantId) {
    return inventoryService.listWarehouses(tenantId);
  }

  @Operation(summary = "Create a warehouse")
  @PostMapping("/warehouses")
  @PreAuthorize("hasAuthority('inventory:manage')")
  public ResponseEntity<WarehouseResponse> createWarehouse(
      @RequestParam String tenantId, @RequestBody WarehouseRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(inventoryService.createWarehouse(tenantId, req));
  }

  // ── Stock levels ───────────────────────────────────────────────────────────

  @Operation(summary = "Get stock levels for a product across all warehouses")
  @GetMapping("/stock/products/{productId}")
  public List<StockLevelResponse> getProductStock(@PathVariable Long productId) {
    return inventoryService.getProductStock(productId);
  }

  @Operation(summary = "Get all stock levels in a warehouse")
  @GetMapping("/stock/warehouses/{warehouseId}")
  public List<StockLevelResponse> getWarehouseStock(@PathVariable Long warehouseId) {
    return inventoryService.getWarehouseStock(warehouseId);
  }

  @Operation(
      summary = "Adjust stock",
      description =
          "Adds or removes stock for a product-warehouse combination. Records a movement.")
  @PostMapping("/stock/adjust")
  @PreAuthorize("hasAuthority('inventory:manage')")
  public StockLevelResponse adjustStock(
      @RequestParam String tenantId, @RequestBody AdjustStockRequest req) {
    return inventoryService.adjustStock(tenantId, req);
  }

  @Operation(
      summary = "Low-stock alert",
      description = "Returns all stock levels at or below the given threshold (default 5).")
  @GetMapping("/stock/low")
  public List<StockLevelResponse> getLowStock(
      @RequestParam String tenantId, @RequestParam(defaultValue = "5") int threshold) {
    return inventoryService.getLowStock(tenantId, threshold);
  }

  @Operation(
      summary = "Set absolute stock level",
      description =
          "Overwrites current stock with a fixed value. Calculates delta and records a movement.")
  @PostMapping("/stock/set")
  @PreAuthorize("hasAuthority('inventory:manage')")
  public StockLevelResponse setStock(
      @RequestParam String tenantId,
      @RequestParam Long productId,
      @RequestParam Long warehouseId,
      @RequestParam int qty) {
    return inventoryService.setStock(tenantId, productId, warehouseId, qty);
  }
}
