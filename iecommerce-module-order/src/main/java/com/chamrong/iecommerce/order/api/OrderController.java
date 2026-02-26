package com.chamrong.iecommerce.order.api;

import com.chamrong.iecommerce.order.application.OrderService;
import com.chamrong.iecommerce.order.application.dto.AddItemRequest;
import com.chamrong.iecommerce.order.application.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * Order lifecycle management.
 *
 * <p>State machine: {@code AddingItems → Confirmed → Shipped → Completed} with a {@code Cancelled}
 * terminal state reachable from any non-terminal state.
 *
 * <p>Base path: {@code /api/v1/orders}
 */
@Tag(name = "Orders", description = "Order creation and lifecycle management")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderController {

  private final OrderService orderService;

  @Operation(summary = "Create a draft order")
  @PostMapping
  public ResponseEntity<OrderResponse> createOrder() {
    return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createDraftOrder());
  }

  @Operation(summary = "Get order by ID")
  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
    return orderService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Add item to draft order")
  @PostMapping("/{id}/items")
  public OrderResponse addItem(@PathVariable Long id, @Valid @RequestBody AddItemRequest req) {
    return orderService.addItem(id, req);
  }

  @Operation(summary = "Apply voucher code")
  @PostMapping("/{id}/apply-voucher")
  public OrderResponse applyVoucher(@PathVariable Long id, @RequestParam String code) {
    return orderService.applyVoucher(id, code);
  }

  @Operation(
      summary = "Confirm order",
      description = "Transitions order from AddingItems → Confirmed.")
  @PostMapping("/{id}/confirm")
  public OrderResponse confirm(@PathVariable Long id) {
    return orderService.confirm(id);
  }

  @Operation(summary = "Pick order items")
  @PostMapping("/{id}/pick")
  @PreAuthorize("hasAuthority('orders:manage')")
  public OrderResponse pick(@PathVariable Long id) {
    return orderService.pick(id);
  }

  @Operation(summary = "Pack order")
  @PostMapping("/{id}/pack")
  @PreAuthorize("hasAuthority('orders:manage')")
  public OrderResponse pack(@PathVariable Long id) {
    return orderService.pack(id);
  }

  @Operation(
      summary = "Mark order as shipped",
      description = "Transitions order from Packing → Shipped.")
  @PostMapping("/{id}/ship")
  @PreAuthorize("hasAuthority('orders:manage')")
  public OrderResponse ship(
      @PathVariable Long id, @RequestParam(required = false) String trackingNumber) {
    return orderService.ship(id, trackingNumber);
  }

  @Operation(summary = "Mark order as delivered")
  @PostMapping("/{id}/deliver")
  @PreAuthorize("hasAuthority('orders:manage')")
  public OrderResponse deliver(@PathVariable Long id) {
    return orderService.deliver(id);
  }

  @Operation(
      summary = "Complete order",
      description = "Transitions order from Delivered → Completed and fires OrderCompletedEvent.")
  @PostMapping("/{id}/complete")
  @PreAuthorize("hasAuthority('orders:manage')")
  public OrderResponse complete(@PathVariable Long id) {
    return orderService.complete(id);
  }

  @Operation(
      summary = "Cancel order",
      description = "Moves order to Cancelled from any non-terminal state.")
  @PostMapping("/{id}/cancel")
  public OrderResponse cancel(@PathVariable Long id) {
    return orderService.cancel(id);
  }
}
