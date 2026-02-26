package com.chamrong.iecommerce.sale.api;

import com.chamrong.iecommerce.sale.application.QuotationService;
import com.chamrong.iecommerce.sale.application.dto.QuotationResponse;
import com.chamrong.iecommerce.sale.domain.Quotation;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales/quotations")
@RequiredArgsConstructor
public class QuotationController {

  private final QuotationService quotationService;

  @PostMapping
  public ResponseEntity<QuotationResponse> createQuotation(
      @RequestHeader("X-Tenant-Id") String tenantId,
      @RequestParam String customerId,
      @RequestParam(required = false) Instant expiryDate,
      @RequestParam(defaultValue = "USD") String currency) {
    return ResponseEntity.ok(
        quotationService.createQuotation(tenantId, customerId, expiryDate, currency));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<QuotationResponse> updateStatus(
      @PathVariable Long id, @RequestParam Quotation.QuotationStatus status) {
    return ResponseEntity.ok(quotationService.updateStatus(id, status));
  }
}
