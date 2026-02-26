package com.chamrong.iecommerce.sale.api;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.sale.application.ReturnService;
import com.chamrong.iecommerce.sale.application.dto.ReturnResponse;
import com.chamrong.iecommerce.sale.domain.SaleReturn;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales/returns")
@RequiredArgsConstructor
public class ReturnController {

  private final ReturnService returnService;

  @PostMapping
  public ResponseEntity<ReturnResponse> createReturn(
      @RequestHeader("X-Tenant-Id") String tenantId,
      @RequestParam String orderId,
      @RequestParam String reason,
      @RequestBody Money refundAmount) {
    return ResponseEntity.ok(returnService.createReturn(tenantId, orderId, reason, refundAmount));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<ReturnResponse> updateStatus(
      @PathVariable Long id, @RequestParam SaleReturn.ReturnStatus status) {
    return ResponseEntity.ok(returnService.updateStatus(id, status));
  }
}
