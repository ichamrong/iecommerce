package com.chamrong.iecommerce.sale.api;

import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.sale.application.SaleService;
import com.chamrong.iecommerce.sale.application.dto.SaleSessionResponse;
import com.chamrong.iecommerce.sale.application.dto.ShiftResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@PreAuthorize(
    "hasAuthority('"
        + Permissions.SALE_READ
        + "') or hasAuthority('"
        + Permissions.SALE_MANAGE
        + "')")
public class SaleController {

  private final SaleService saleService;

  @PostMapping("/shifts")
  @PreAuthorize("hasAuthority('" + Permissions.SALE_MANAGE + "')")
  public ResponseEntity<ShiftResponse> startShift(
      @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
      @RequestParam @NotBlank String staffId,
      @RequestParam @NotBlank String terminalId,
      @RequestParam @Min(0) BigDecimal openingBalance) {
    return ResponseEntity.ok(saleService.startShift(tenantId, staffId, terminalId, openingBalance));
  }

  @PatchMapping("/shifts/{id}/close")
  @PreAuthorize("hasAuthority('" + Permissions.SALE_MANAGE + "')")
  public ResponseEntity<ShiftResponse> closeShift(
      @PathVariable @NotNull Long id, @RequestParam @Min(0) BigDecimal closingBalance) {
    return ResponseEntity.ok(saleService.closeShift(id, closingBalance));
  }

  @PostMapping("/sessions")
  @PreAuthorize("hasAuthority('" + Permissions.SALE_MANAGE + "')")
  public ResponseEntity<SaleSessionResponse> startSession(
      @RequestHeader("X-Tenant-Id") String tenantId,
      @RequestParam Long shiftId,
      @RequestParam(required = false) String reference,
      @RequestParam(required = false) String customerId) {
    return ResponseEntity.ok(saleService.startSession(tenantId, shiftId, reference, customerId));
  }

  @PatchMapping("/sessions/{id}/end")
  @PreAuthorize("hasAuthority('" + Permissions.SALE_MANAGE + "')")
  public ResponseEntity<SaleSessionResponse> endSession(@PathVariable Long id) {
    return ResponseEntity.ok(saleService.endSession(id));
  }
}
