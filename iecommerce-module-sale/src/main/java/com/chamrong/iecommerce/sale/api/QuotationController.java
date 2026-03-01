package com.chamrong.iecommerce.sale.api;

import com.chamrong.iecommerce.common.dto.CursorPage;
import com.chamrong.iecommerce.sale.application.command.CreateQuotationCommand;
import com.chamrong.iecommerce.sale.application.dto.QuotationResponse;
import com.chamrong.iecommerce.sale.application.query.SaleQueryService;
import com.chamrong.iecommerce.sale.application.usecase.QuotationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales/quotations")
@RequiredArgsConstructor
public class QuotationController {

  private final QuotationUseCase quotationUseCase;
  private final SaleQueryService queryService;

  @PostMapping
  public ResponseEntity<QuotationResponse> createQuotation(
      @RequestBody @Valid CreateQuotationCommand command,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
    return ResponseEntity.ok(quotationUseCase.createQuotation(command, idempotencyKey));
  }

  @GetMapping
  public ResponseEntity<CursorPage<QuotationResponse>> listQuotations(
      @RequestHeader("X-Tenant-Id") String tenantId,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {
    return ResponseEntity.ok(queryService.listQuotations(tenantId, cursor, limit));
  }

  @PatchMapping("/{id}/confirm")
  public ResponseEntity<QuotationResponse> confirmQuotation(
      @PathVariable Long id,
      @RequestHeader("X-Tenant-Id") String tenantId,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
    return ResponseEntity.ok(quotationUseCase.confirmQuotation(id, tenantId, idempotencyKey));
  }

  @PatchMapping("/{id}/cancel")
  public ResponseEntity<QuotationResponse> cancelQuotation(
      @PathVariable Long id, @RequestHeader("X-Tenant-Id") String tenantId) {
    return ResponseEntity.ok(quotationUseCase.cancelQuotation(id, tenantId));
  }
}
