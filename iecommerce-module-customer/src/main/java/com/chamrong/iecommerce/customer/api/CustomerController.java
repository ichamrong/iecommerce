package com.chamrong.iecommerce.customer.api;

import com.chamrong.iecommerce.customer.application.command.AddAddressHandler;
import com.chamrong.iecommerce.customer.application.command.BlockCustomerHandler;
import com.chamrong.iecommerce.customer.application.command.CreateCustomerCommand;
import com.chamrong.iecommerce.customer.application.command.CreateCustomerHandler;
import com.chamrong.iecommerce.customer.application.command.RemoveAddressHandler;
import com.chamrong.iecommerce.customer.application.command.UnblockCustomerHandler;
import com.chamrong.iecommerce.customer.application.command.UpdateCustomerHandler;
import com.chamrong.iecommerce.customer.application.dto.AddAddressRequest;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.application.dto.UpdateCustomerRequest;
import com.chamrong.iecommerce.customer.application.query.CustomerQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Admin — Customers", description = "Customer profile management (admin)")
@RestController
@RequestMapping("/api/v1/admin/customers")
public class CustomerController {

  private final CreateCustomerHandler createCustomerHandler;
  private final UpdateCustomerHandler updateCustomerHandler;
  private final BlockCustomerHandler blockCustomerHandler;
  private final UnblockCustomerHandler unblockCustomerHandler;
  private final AddAddressHandler addAddressHandler;
  private final RemoveAddressHandler removeAddressHandler;
  private final CustomerQueryHandler customerQueryHandler;

  public CustomerController(
      CreateCustomerHandler createCustomerHandler,
      UpdateCustomerHandler updateCustomerHandler,
      BlockCustomerHandler blockCustomerHandler,
      UnblockCustomerHandler unblockCustomerHandler,
      AddAddressHandler addAddressHandler,
      RemoveAddressHandler removeAddressHandler,
      CustomerQueryHandler customerQueryHandler) {
    this.createCustomerHandler = createCustomerHandler;
    this.updateCustomerHandler = updateCustomerHandler;
    this.blockCustomerHandler = blockCustomerHandler;
    this.unblockCustomerHandler = unblockCustomerHandler;
    this.addAddressHandler = addAddressHandler;
    this.removeAddressHandler = removeAddressHandler;
    this.customerQueryHandler = customerQueryHandler;
  }

  // ── Reads ────────────────────────────────────────────────────────────────

  @Operation(summary = "List all customers")
  @GetMapping
  public ResponseEntity<List<CustomerResponse>> getAllCustomers(
      @RequestHeader("X-Tenant-ID") String tenantId) {
    return ResponseEntity.ok(customerQueryHandler.findAll(tenantId));
  }

  @Operation(summary = "Get customer by ID")
  @GetMapping("/{id}")
  public ResponseEntity<CustomerResponse> getCustomerById(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable Long id) {
    return ResponseEntity.ok(customerQueryHandler.findById(tenantId, id));
  }

  // ── Writes ───────────────────────────────────────────────────────────────

  @Operation(summary = "Create customer")
  @PostMapping
  public ResponseEntity<CustomerResponse> createCustomer(
      @RequestHeader("X-Tenant-ID") String tenantId, @RequestBody CreateCustomerCommand command) {
    CustomerResponse response = createCustomerHandler.handle(tenantId, command);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
    return ResponseEntity.created(location).body(response);
  }

  @Operation(summary = "Update customer")
  @PutMapping("/{id}")
  public ResponseEntity<CustomerResponse> updateCustomer(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable Long id,
      @RequestBody UpdateCustomerRequest request) {
    return ResponseEntity.ok(updateCustomerHandler.handle(tenantId, id, request));
  }

  // ── Addresses ────────────────────────────────────────────────────────────

  @Operation(summary = "Add Address")
  @PostMapping("/{id}/addresses")
  public ResponseEntity<CustomerResponse> addAddress(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable Long id,
      @RequestBody AddAddressRequest request) {
    return ResponseEntity.ok(addAddressHandler.handle(tenantId, id, request));
  }

  @Operation(summary = "Remove Address")
  @DeleteMapping("/{id}/addresses/{addressId}")
  public ResponseEntity<CustomerResponse> removeAddress(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable Long id,
      @PathVariable Long addressId) {
    return ResponseEntity.ok(removeAddressHandler.handle(tenantId, id, addressId));
  }

  // ── Lifecycle ────────────────────────────────────────────────────────────

  @Operation(summary = "Block a customer")
  @PatchMapping("/{id}/block")
  public ResponseEntity<Void> blockCustomer(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable Long id) {
    blockCustomerHandler.handle(tenantId, id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Unblock a customer")
  @PatchMapping("/{id}/unblock")
  public ResponseEntity<Void> unblockCustomer(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable Long id) {
    unblockCustomerHandler.handle(tenantId, id);
    return ResponseEntity.noContent().build();
  }
}
