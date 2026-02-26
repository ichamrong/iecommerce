package com.chamrong.iecommerce.invoice.api;

import com.chamrong.iecommerce.invoice.application.PosReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "POS Receipt API", description = "Point of sale terminal thermal receipts")
@RestController
@RequestMapping("/api/v1/tenants/me/pos/receipts")
@RequiredArgsConstructor
public class PosReceiptController {

  private final PosReceiptService posReceiptService;

  @Operation(summary = "Generate Thermal Receipt")
  @GetMapping(value = "/{invoiceId}/thermal", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> generateThermalReceipt(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long invoiceId,
      @RequestParam Long terminalId) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    String textReceipt = posReceiptService.generateThermalReceipt(tenantId, invoiceId, terminalId);
    return ResponseEntity.ok(textReceipt);
  }
}
