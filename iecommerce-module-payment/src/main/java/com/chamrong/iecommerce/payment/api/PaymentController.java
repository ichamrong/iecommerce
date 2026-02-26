package com.chamrong.iecommerce.payment.api;

import com.chamrong.iecommerce.payment.application.PaymentService;
import com.chamrong.iecommerce.payment.application.dto.PaymentRequest;
import com.chamrong.iecommerce.payment.application.dto.PaymentResponse;
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
 * Payment transaction management.
 *
 * <p>Base path: {@code /api/v1/payments}
 */
@Tag(name = "Payments", description = "Payment initiation and lifecycle management")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PaymentController {

  private final PaymentService paymentService;

  @Operation(summary = "Initiate a payment for an order")
  @PostMapping
  public ResponseEntity<PaymentResponse> initiate(
      @RequestParam String tenantId, @RequestBody PaymentRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiate(tenantId, req));
  }

  @Operation(summary = "Get payment by ID")
  @GetMapping("/{id}")
  public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
    return paymentService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get payments for an order")
  @GetMapping("/orders/{orderId}")
  public List<PaymentResponse> getByOrder(@PathVariable Long orderId) {
    return paymentService.findByOrderId(orderId);
  }

  @Operation(
      summary = "Mark payment as succeeded",
      description = "Call this after the gateway webhook confirms the charge.")
  @PostMapping("/{id}/succeed")
  @PreAuthorize("hasAuthority('payments:manage')")
  public PaymentResponse succeed(@PathVariable Long id, @RequestParam String externalId) {
    return paymentService.markSucceeded(id, externalId);
  }

  @Operation(summary = "Mark payment as failed")
  @PostMapping("/{id}/fail")
  @PreAuthorize("hasAuthority('payments:manage')")
  public PaymentResponse fail(@PathVariable Long id) {
    return paymentService.markFailed(id);
  }

  @Operation(summary = "Refund a succeeded payment")
  @PostMapping("/{id}/refund")
  @PreAuthorize("hasAuthority('payments:manage')")
  public PaymentResponse refund(@PathVariable Long id) {
    return paymentService.refund(id);
  }
}
