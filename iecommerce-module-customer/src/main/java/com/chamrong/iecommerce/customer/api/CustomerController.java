package com.chamrong.iecommerce.customer.api;

import com.chamrong.iecommerce.customer.application.command.CreateCustomerCommand;
import com.chamrong.iecommerce.customer.application.command.CreateCustomerHandler;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.application.query.CustomerQueryHandler;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('profile:read')")
  public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
    return ResponseEntity.ok(customerQueryHandler.findById(id));
  }

  @GetMapping("/auth/{authUserId}")
  @PreAuthorize("hasAuthority('profile:read')")
  public ResponseEntity<CustomerResponse> getCustomerByAuthUserId(@PathVariable Long authUserId) {
    return ResponseEntity.ok(customerQueryHandler.findByAuthUserId(authUserId));
  }

  @GetMapping
  @PreAuthorize("hasAuthority('user:read')")
  public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
    return ResponseEntity.ok(customerQueryHandler.findAll());
  }
}
