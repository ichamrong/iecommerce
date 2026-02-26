package com.chamrong.iecommerce.report.api;

import com.chamrong.iecommerce.report.application.PosReconciliationDto;
import com.chamrong.iecommerce.report.application.ReconciliationReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "POS Reports API", description = "Point of sale end-of-day reports (Phase 5)")
@RestController
@RequestMapping("/api/v1/tenants/me/pos/reports")
@RequiredArgsConstructor
public class PosReportController {

  private final ReconciliationReportService reconciliationReportService;

  @Operation(summary = "Generate Cash Drawer Reconciliation Report")
  @GetMapping("/reconciliation")
  public ResponseEntity<PosReconciliationDto> getReconciliationReport(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam Long terminalId,
      @RequestParam Long cashierId,
      @RequestParam BigDecimal actualCashCounted) {
    String tenantId = jwt.getClaimAsString("tenant_id");

    // In a real environment, actualCashCounted would be passed securely or validated.
    PosReconciliationDto report =
        reconciliationReportService.generateSessionReport(
            tenantId, terminalId, cashierId, actualCashCounted);

    return ResponseEntity.ok(report);
  }
}
