package com.chamrong.iecommerce.customer.api;

import com.chamrong.iecommerce.customer.application.command.CreateCustomerCommand;
import com.chamrong.iecommerce.customer.application.command.CreateCustomerHandler;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.application.query.CustomerQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Customers", description = "Customer profile management within a tenant")
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

  private final CreateCustomerHandler createCustomerHandler;
  private final CustomerQueryHandler customerQueryHandler;

  public CustomerController(
      CreateCustomerHandler createCustomerHandler, CustomerQueryHandler customerQueryHandler) {
    this.createCustomerHandler = createCustomerHandler;
    this.customerQueryHandler = customerQueryHandler;
  }

  @Operation(
      summary = "Create customer",
      description =
          "Creates a new customer profile linked to an auth user. Requires `user:create`"
              + " permission.")
  @PostMapping
  @PreAuthorize("hasAuthority('user:create')")
  public ResponseEntity<CustomerResponse> createCustomer(
      @RequestBody CreateCustomerCommand command) {
    CustomerResponse response = createCustomerHandler.handle(command);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
    return ResponseEntity.created(location).body(response);
  }

  @Operation(
      summary = "Get customer by ID",
      description =
          "Fetch a customer profile by their local ID. Requires `profile:read` permission.")
  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('profile:read')")
  public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
    return ResponseEntity.ok(customerQueryHandler.findById(id));
  }

  @Operation(
      summary = "Get customer by auth user ID",
      description =
          "Fetch a customer profile by their Keycloak/auth user ID. Requires `profile:read`"
              + " permission.")
  @GetMapping("/auth/{authUserId}")
  @PreAuthorize("hasAuthority('profile:read')")
  public ResponseEntity<CustomerResponse> getCustomerByAuthUserId(@PathVariable Long authUserId) {
    return ResponseEntity.ok(customerQueryHandler.findByAuthUserId(authUserId));
  }

  @Operation(
      summary = "List all customers",
      description = "Returns all customers in the current tenant. Requires `user:read` permission.")
  @GetMapping
  @PreAuthorize("hasAuthority('user:read')")
  public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
    return ResponseEntity.ok(customerQueryHandler.findAll());
  }
}
