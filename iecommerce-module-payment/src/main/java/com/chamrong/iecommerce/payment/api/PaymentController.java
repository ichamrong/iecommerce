package com.chamrong.iecommerce.payment.api;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.payment.application.command.CreatePaymentIntentHandler;
import com.chamrong.iecommerce.payment.domain.PaymentIntent;
import com.chamrong.iecommerce.payment.domain.ProviderType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment API", description = "High-level entry for payment operations")
public class PaymentController {

  private final CreatePaymentIntentHandler createHandler;
  private final com.chamrong.iecommerce.payment.application.query.ListPaymentIntentsHandler
      listHandler;

  @Operation(
      summary = "Create a payment intent",
      description = "Initializes a payment with a provider and returns the intent details.")
  @PostMapping("/intents")
  public ResponseEntity<PaymentIntentDto> createIntent(
      @RequestHeader("X-Tenant-Id") String tenantId,
      @jakarta.validation.Valid @RequestBody CreateIntentRequest request) {

    PaymentIntent intent =
        createHandler.handle(
            new CreatePaymentIntentHandler.Command(
                tenantId,
                request.orderId(),
                new Money(request.amount(), request.currency()),
                request.provider(),
                request.idempotencyKey(),
                request.returnUrl(),
                request.cancelUrl()));

    return ResponseEntity.ok(toDto(intent));
  }

  @Operation(
      summary = "Get payment history",
      description = "Retrieves a page of payment intents using keyset pagination.")
  @GetMapping("/history")
  public ResponseEntity<PaymentPageDto> getHistory(
      @RequestHeader("X-Tenant-Id") String tenantId,
      @RequestParam(required = false) java.time.Instant cursorTime,
      @RequestParam(required = false) java.util.UUID cursorId,
      @RequestParam(defaultValue = "20") int limit) {

    var intents =
        listHandler.handle(
            new com.chamrong.iecommerce.payment.application.query.ListPaymentIntentsHandler.Query(
                tenantId, cursorTime, cursorId, limit));

    String nextCursorTime = null;
    String nextCursorId = null;
    if (!intents.isEmpty()) {
      var last = intents.get(intents.size() - 1);
      nextCursorTime = last.getCreatedAt().toString();
      nextCursorId = last.getIntentId().toString();
    }

    return ResponseEntity.ok(
        new PaymentPageDto(
            intents.stream().map(this::toDto).toList(), nextCursorTime, nextCursorId));
  }

  public record CreateIntentRequest(
      @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive Long orderId,
      @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive
          java.math.BigDecimal amount,
      @jakarta.validation.constraints.NotBlank
          @jakarta.validation.constraints.Size(min = 3, max = 3)
          String currency,
      @jakarta.validation.constraints.NotNull ProviderType provider,
      @jakarta.validation.constraints.NotBlank
          @jakarta.validation.constraints.Size(min = 8, max = 128)
          String idempotencyKey,
      @jakarta.validation.constraints.NotBlank String returnUrl,
      @jakarta.validation.constraints.NotBlank String cancelUrl) {}

  public record PaymentIntentDto(
      @io.swagger.v3.oas.annotations.media.Schema(description = "Internal unique intent ID")
          String intentId,
      @io.swagger.v3.oas.annotations.media.Schema(description = "Associated order ID") Long orderId,
      @io.swagger.v3.oas.annotations.media.Schema(description = "Payment amount")
          java.math.BigDecimal amount,
      @io.swagger.v3.oas.annotations.media.Schema(description = "Currency code (e.g. USD)")
          String currency,
      @io.swagger.v3.oas.annotations.media.Schema(description = "Provider type") String provider,
      @io.swagger.v3.oas.annotations.media.Schema(description = "Current payment status")
          String status,
      @io.swagger.v3.oas.annotations.media.Schema(description = "External ID from provider")
          String externalId,
      @io.swagger.v3.oas.annotations.media.Schema(description = "Checkout URL for redirection")
          String checkoutUrl,
      @io.swagger.v3.oas.annotations.media.Schema(description = "Client secret for mobile SDKs")
          String clientSecret,
      @io.swagger.v3.oas.annotations.media.Schema(description = "Creation timestamp")
          String createdAt) {}

  public record PaymentPageDto(
      List<PaymentIntentDto> items, String nextCursorTime, String nextCursorId) {}

  private PaymentIntentDto toDto(PaymentIntent p) {
    return new PaymentIntentDto(
        p.getIntentId().toString(),
        p.getOrderId(),
        p.getAmount().getAmount(),
        p.getAmount().getCurrency(),
        p.getProvider().name(),
        p.getStatus().name(),
        p.getExternalId(),
        p.getCheckoutUrl(),
        p.getClientSecret(),
        p.getCreatedAt().toString());
  }
}
