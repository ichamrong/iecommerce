package com.chamrong.iecommerce.payment.api;

import com.chamrong.iecommerce.payment.application.FinancialLedgerService;
import com.chamrong.iecommerce.payment.domain.FinancialLedger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Financial Ledger",
    description =
        "Admin endpoints for overseeing ledger operations and processing manual payouts/refunds")
@RestController
@RequestMapping("/api/v1/finance/ledgers")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FinancialLedgerController {

  private final FinancialLedgerService ledgerService;

  @Operation(summary = "Get all pending payouts and refunds")
  @GetMapping("/pending")
  @PreAuthorize("hasAuthority('finance:manage')")
  public ResponseEntity<List<FinancialLedger>> getPendingPayouts() {
    return ResponseEntity.ok(ledgerService.getPendingPayouts());
  }

  @Operation(
      summary = "Execute a manual payout/refund",
      description = "Marks a PENDING ledger entry as EXECUTED with reference details.")
  @PostMapping("/{id}/execute")
  @PreAuthorize("hasAuthority('finance:manage')")
  public ResponseEntity<FinancialLedger> executePayout(
      @PathVariable Long id, @jakarta.validation.Valid @RequestBody ExecutePayoutRequest request) {
    return ResponseEntity.ok(
        ledgerService.executePayout(id, request.adminReferenceId(), request.bankTransactionId()));
  }

  public record ExecutePayoutRequest(
      @jakarta.validation.constraints.NotBlank
          @io.swagger.v3.oas.annotations.media.Schema(
              description = "Internal admin reference ID for the payout")
          String adminReferenceId,
      @jakarta.validation.constraints.NotBlank
          @io.swagger.v3.oas.annotations.media.Schema(description = "External bank transaction ID")
          String bankTransactionId) {}
}
