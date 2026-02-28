package com.chamrong.iecommerce.order.api;

import com.chamrong.iecommerce.order.application.command.*;
import com.chamrong.iecommerce.order.application.dto.*;
import com.chamrong.iecommerce.order.application.query.OrderQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Order Module API — Hexagonal Architecture Implementation. */
@Tag(name = "Orders", description = "Order creation and lifecycle management")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderController {

  private final CreateOrderHandler createOrderHandler;
  private final AddItemHandler addItemHandler;
  private final ConfirmOrderHandler confirmOrderHandler;
  private final ApplyVoucherHandler applyVoucherHandler;
  private final PickOrderHandler pickOrderHandler;
  private final PackOrderHandler packOrderHandler;
  private final ShipOrderHandler shipOrderHandler;
  private final DeliverOrderHandler deliverOrderHandler;
  private final CompleteOrderHandler completeOrderHandler;
  private final CancelOrderHandler cancelOrderHandler;
  private final OrderQueryHandler queryHandler;

  @Operation(summary = "Create a draft order")
  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(Principal principal) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(createOrderHandler.handle(principal.getName()));
  }

  @Operation(summary = "Get order summary list (Keyset Paginated)")
  @GetMapping
  public OrderCursorResponse<OrderSummaryResponse> listOrders(
      @RequestParam String tenantId,
      @RequestParam Long customerId,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {
    return queryHandler.listByCustomer(tenantId, customerId, cursor, limit);
  }

  @Operation(summary = "Get order audit history (Keyset Paginated)")
  @GetMapping("/{id}/audit")
  public OrderCursorResponse<AuditLogResponse> getAuditLog(
      @PathVariable Long id,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {
    return queryHandler.listAuditLog(id, cursor, limit);
  }

  @Operation(summary = "Add item to draft order")
  @PostMapping("/{id}/items")
  public OrderResponse addItem(
      @PathVariable Long id, @Valid @RequestBody AddItemRequest req, Principal principal) {
    return addItemHandler.handle(id, req, principal.getName());
  }

  @Operation(summary = "Apply voucher code")
  @PostMapping("/{id}/apply-voucher")
  public OrderResponse applyVoucher(
      @PathVariable Long id, @RequestParam String code, Principal principal) {
    return applyVoucherHandler.handle(id, code, principal.getName());
  }

  @Operation(summary = "Confirm order (Idempotent)")
  @PostMapping("/{id}/confirm")
  public OrderResponse confirm(
      @PathVariable Long id,
      @RequestHeader("idempotency-key") String requestId,
      Principal principal) {
    return confirmOrderHandler.handle(id, requestId, principal.getName());
  }

  @Operation(summary = "Pick order items")
  @PostMapping("/{id}/pick")
  @PreAuthorize("hasAuthority('orders:manage')")
  public OrderResponse pick(@PathVariable Long id, Principal principal) {
    return pickOrderHandler.handle(id, principal.getName());
  }

  @Operation(summary = "Pack order")
  @PostMapping("/{id}/pack")
  @PreAuthorize("hasAuthority('orders:manage')")
  public OrderResponse pack(@PathVariable Long id, Principal principal) {
    return packOrderHandler.handle(id, principal.getName());
  }

  @Operation(summary = "Mark order as shipped (Idempotent)")
  @PostMapping("/{id}/ship")
  @PreAuthorize("hasAuthority('orders:manage')")
  public OrderResponse ship(
      @PathVariable Long id, @Valid @RequestBody ShipOrderRequest req, Principal principal) {
    return shipOrderHandler.handle(id, req.trackingNumber(), req.requestId(), principal.getName());
  }

  @Operation(summary = "Mark order as delivered")
  @PostMapping("/{id}/deliver")
  @PreAuthorize("hasAuthority('orders:manage')")
  public OrderResponse deliver(@PathVariable Long id, Principal principal) {
    return deliverOrderHandler.handle(id, principal.getName());
  }

  @Operation(summary = "Complete order")
  @PostMapping("/{id}/complete")
  @PreAuthorize("hasAuthority('orders:manage')")
  public OrderResponse complete(@PathVariable Long id, Principal principal) {
    return completeOrderHandler.handle(id, principal.getName());
  }

  @Operation(summary = "Cancel order (Idempotent)")
  @PostMapping("/{id}/cancel")
  public OrderResponse cancel(
      @PathVariable Long id,
      @RequestHeader("idempotency-key") String requestId,
      Principal principal) {
    return cancelOrderHandler.handle(id, requestId, principal.getName());
  }
}
