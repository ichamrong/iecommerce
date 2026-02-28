package com.chamrong.iecommerce.inventory.api;

import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.inventory.application.command.AdjustStockHandler;
import com.chamrong.iecommerce.inventory.application.command.CommitReservationHandler;
import com.chamrong.iecommerce.inventory.application.command.ReceiveStockHandler;
import com.chamrong.iecommerce.inventory.application.command.ReleaseReservationHandler;
import com.chamrong.iecommerce.inventory.application.command.ReserveStockHandler;
import com.chamrong.iecommerce.inventory.application.dto.InventoryCursorResponse;
import com.chamrong.iecommerce.inventory.application.dto.LedgerEntryResponse;
import com.chamrong.iecommerce.inventory.application.dto.OnHandResponse;
import com.chamrong.iecommerce.inventory.application.dto.ReservationResponse;
import com.chamrong.iecommerce.inventory.application.dto.ReserveStockRequest;
import com.chamrong.iecommerce.inventory.application.query.InventoryQueryHandler;
import com.chamrong.iecommerce.inventory.domain.StockReservation.ReservationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inventory management API.
 *
 * <p>Exposes stock operations and read queries for warehouse staff and internal system use. All
 * mutating endpoints are transactional and idempotent by {@code referenceId}.
 */
@Tag(name = "Inventory", description = "Stock management — ledger, projections, reservations")
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class InventoryController {

  private final ReceiveStockHandler receiveHandler;
  private final AdjustStockHandler adjustHandler;
  private final ReserveStockHandler reserveHandler;
  private final CommitReservationHandler commitHandler;
  private final ReleaseReservationHandler releaseHandler;
  private final InventoryQueryHandler queryHandler;

  // ── On-Hand ──────────────────────────────────────────────────────────────

  @Operation(summary = "Get on-hand stock for a product (all warehouses)")
  @GetMapping("/products/{productId}/on-hand")
  public List<OnHandResponse> getOnHand(@PathVariable Long productId) {
    return queryHandler.getOnHand(TenantContext.requireTenantId(), productId);
  }

  @Operation(summary = "Get on-hand stock for a specific product–warehouse pair")
  @GetMapping("/products/{productId}/warehouses/{warehouseId}/on-hand")
  public ResponseEntity<OnHandResponse> getOnHandByWarehouse(
      @PathVariable Long productId, @PathVariable Long warehouseId) {
    return queryHandler
        .getOnHandByWarehouse(TenantContext.requireTenantId(), productId, warehouseId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // ── Ledger History ────────────────────────────────────────────────────────

  @Operation(summary = "Get cursor-paginated ledger history for a product")
  @GetMapping("/products/{productId}/ledger")
  public InventoryCursorResponse<LedgerEntryResponse> getLedger(
      @PathVariable Long productId,
      @Parameter(description = "Optional warehouse filter") @RequestParam(required = false)
          Long warehouseId,
      @Parameter(description = "Opaque cursor from previous response")
          @RequestParam(required = false)
          String cursor,
      @Parameter(description = "Page size (1–100, default 20)") @RequestParam(defaultValue = "20")
          int limit) {
    return queryHandler.getLedgerHistory(
        TenantContext.requireTenantId(), productId, warehouseId, cursor, limit);
  }

  // ── Reservations ──────────────────────────────────────────────────────────

  @Operation(summary = "Get cursor-paginated reservations for a product")
  @GetMapping("/products/{productId}/reservations")
  public InventoryCursorResponse<ReservationResponse> getReservations(
      @PathVariable Long productId,
      @Parameter(description = "Optional status filter") @RequestParam(required = false)
          ReservationStatus status,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {
    return queryHandler.getReservations(
        TenantContext.requireTenantId(), productId, status, cursor, limit);
  }

  // ── Stock Operations ──────────────────────────────────────────────────────

  @Operation(summary = "Receive stock (inbound delivery)")
  @PostMapping("/products/{productId}/warehouses/{warehouseId}/receive")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('INVENTORY_MANAGER')")
  public void receiveStock(
      @PathVariable Long productId,
      @PathVariable Long warehouseId,
      @RequestParam @Positive int qty,
      @RequestParam @NotBlank String referenceId,
      @RequestParam(required = false, defaultValue = "SYSTEM") String actorId,
      @RequestParam(required = false) String metadata) {
    receiveHandler.handle(
        TenantContext.requireTenantId(),
        productId,
        warehouseId,
        qty,
        referenceId,
        actorId,
        metadata);
  }

  @Operation(summary = "Adjust stock (manual correction)")
  @PostMapping("/products/{productId}/warehouses/{warehouseId}/adjust")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('INVENTORY_MANAGER')")
  public void adjustStock(
      @PathVariable Long productId,
      @PathVariable Long warehouseId,
      @RequestParam int delta,
      @RequestParam @NotBlank String referenceId,
      @RequestParam @NotBlank String reason,
      @RequestParam(required = false, defaultValue = "SYSTEM") String actorId,
      @RequestParam(defaultValue = "false") boolean allowNegative,
      @RequestParam(required = false) String metadata) {
    adjustHandler.handle(
        TenantContext.requireTenantId(),
        productId,
        warehouseId,
        delta,
        referenceId,
        reason,
        actorId,
        allowNegative,
        metadata);
  }

  @Operation(summary = "Reserve stock for an order")
  @PostMapping("/products/{productId}/warehouses/{warehouseId}/reserve")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void reserveStock(
      @PathVariable Long productId,
      @PathVariable Long warehouseId,
      @RequestParam @Positive int qty,
      @RequestParam @NotBlank String referenceType,
      @RequestParam @NotBlank String referenceId,
      @RequestParam(required = false, defaultValue = "SYSTEM") String actorId,
      @RequestParam(required = false) Instant expiresAt) {
    reserveHandler.handle(
        new ReserveStockRequest(
            TenantContext.requireTenantId(),
            productId,
            warehouseId,
            qty,
            referenceType,
            referenceId,
            actorId,
            expiresAt));
  }

  @Operation(summary = "Commit an existing reservation")
  @PostMapping("/reservations/commit")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void commitReservation(
      @RequestParam @NotBlank String referenceType,
      @RequestParam @NotBlank String referenceId,
      @RequestParam(required = false, defaultValue = "SYSTEM") String actorId) {
    commitHandler.handle(TenantContext.requireTenantId(), referenceType, referenceId, actorId);
  }

  @Operation(summary = "Release an existing reservation")
  @PostMapping("/reservations/release")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void releaseReservation(
      @RequestParam @NotBlank String referenceType,
      @RequestParam @NotBlank String referenceId,
      @RequestParam(required = false, defaultValue = "SYSTEM") String actorId) {
    releaseHandler.handle(TenantContext.requireTenantId(), referenceType, referenceId, actorId);
  }
}
