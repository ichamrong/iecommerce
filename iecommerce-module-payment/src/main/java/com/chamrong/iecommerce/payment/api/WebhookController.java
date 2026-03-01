package com.chamrong.iecommerce.payment.api;

import com.chamrong.iecommerce.payment.application.WebhookVerificationService;
import com.chamrong.iecommerce.payment.application.command.ProcessWebhookHandler;
import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.WebhookVerificationPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Payment Webhooks", description = "Endpoints for provider webhooks")
public class WebhookController {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(WebhookController.class);

  private final WebhookVerificationService verificationService;
  private final ProcessWebhookHandler webhookHandler;

  @Operation(
      summary = "Handle Stripe webhooks",
      description = "Endpoint for processing Stripe payment events.")
  @PostMapping("/stripe")
  public ResponseEntity<String> handleStripe(
      @RequestBody String payload, HttpServletRequest request) {
    return handle(ProviderType.STRIPE, payload, request);
  }

  @Operation(
      summary = "Handle PayPal webhooks",
      description = "Endpoint for processing PayPal payment events.")
  @PostMapping("/paypal")
  public ResponseEntity<String> handlePayPal(
      @RequestBody String payload, HttpServletRequest request) {
    return handle(ProviderType.PAYPAL, payload, request);
  }

  @Operation(
      summary = "Handle ABA webhooks",
      description = "Endpoint for processing ABA payment events.")
  @PostMapping("/aba")
  public ResponseEntity<String> handleABA(@RequestBody String payload, HttpServletRequest request) {
    return handle(ProviderType.ABA, payload, request);
  }

  @Operation(
      summary = "Handle Bakong webhooks",
      description = "Endpoint for processing Bakong payment events.")
  @PostMapping("/bakong")
  public ResponseEntity<String> handleBakong(
      @RequestBody String payload, HttpServletRequest request) {
    return handle(ProviderType.BAKONG, payload, request);
  }

  private ResponseEntity<String> handle(
      ProviderType provider, String payload, HttpServletRequest request) {
    Map<String, String> headers = extractHeaders(request);

    log.debug(
        "{} provider={}",
        com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents.WEBHOOK_RECEIVED,
        provider);

    WebhookVerificationPort.VerificationResult result =
        verificationService.verify(provider, payload, headers);

    if (!result.isValid()) {
      log.warn(
          "{} provider={} description=\"Invalid signature\"",
          com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents
              .WEBHOOK_VERIFICATION_FAILED,
          provider);
      return ResponseEntity.status(400).body("Invalid signature");
    }

    log.info(
        "{} provider={} eventId={} type={}",
        com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents.WEBHOOK_RECEIVED,
        provider,
        result.providerEventId(),
        result.eventType());

    webhookHandler.handle(provider, result);

    return ResponseEntity.ok("success");
  }

  private Map<String, String> extractHeaders(HttpServletRequest request) {
    Map<String, String> headers = new HashMap<>();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      headers.put(name.toLowerCase(), request.getHeader(name));
    }
    return headers;
  }
}
