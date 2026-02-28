package com.chamrong.iecommerce.customer.infrastructure;

import com.chamrong.iecommerce.customer.CustomerApi;
import com.chamrong.iecommerce.customer.api.dto.CursorResponse;
import com.chamrong.iecommerce.customer.application.dto.AddAddressRequest;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.application.dto.UpdateCustomerRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer Management APIs")
public class CustomerController {

  private final CustomerApi customerApi;

  @GetMapping
  @Operation(summary = "List customers with cursor pagination")
  public ResponseEntity<CursorResponse<CustomerResponse>> listCustomers(
      @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "20") int limit) {
    return ResponseEntity.ok(customerApi.listCustomers(cursor, limit));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get full customer profile")
  public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long id) {
    return ResponseEntity.ok(customerApi.getCustomerFull(id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update customer profile")
  public ResponseEntity<CustomerResponse> updateCustomer(
      @PathVariable Long id, @RequestBody UpdateCustomerRequest req) {
    return ResponseEntity.ok(customerApi.updateCustomer(id, req));
  }

  @PatchMapping("/{id}/block")
  @Operation(summary = "Block customer")
  public ResponseEntity<Void> blockCustomer(@PathVariable Long id) {
    customerApi.blockCustomer(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/unblock")
  @Operation(summary = "Unblock customer")
  public ResponseEntity<Void> unblockCustomer(@PathVariable Long id) {
    customerApi.unblockCustomer(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/addresses")
  @Operation(summary = "Add shipping/billing address")
  public ResponseEntity<Void> addAddress(
      @PathVariable Long id, @RequestBody AddAddressRequest req) {
    customerApi.addAddress(id, req);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}/addresses/{addressId}")
  @Operation(summary = "Remove address")
  public ResponseEntity<Void> removeAddress(@PathVariable Long id, @PathVariable Long addressId) {
    customerApi.removeAddress(id, addressId);
    return ResponseEntity.noContent().build();
  }
}
