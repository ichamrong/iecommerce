package com.chamrong.iecommerce.report.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReconciliationReportService {

  /**
   * Generates a reconciliation report for a specific POS terminal session. This bridges Order and
   * Auth modules conceptually, but operates as a stub for Phase 5.
   */
  public PosReconciliationDto generateSessionReport(
      String tenantId, Long terminalId, Long cashierId, BigDecimal actualCashCounted) {

    // Stub implementation for Phase 5 POS infrastructure binding.
    // In production, this would scan the "ecommerce_order" and "payment_transaction" tables
    // filtering by orders placed during the specific auth_pos_session timelines.

    log.info(
        "Generating POS Reconciliation Report for terminal={} cashier={} tenant={}",
        terminalId,
        cashierId,
        tenantId);

    List<PosReconciliationDto.PosTransactionDto> transactions = new ArrayList<>();
    transactions.add(
        PosReconciliationDto.PosTransactionDto.builder()
            .orderId(101L)
            .amount(new BigDecimal("25.50"))
            .method("CASH")
            .timestamp(Instant.now().minusSeconds(3600))
            .build());
    transactions.add(
        PosReconciliationDto.PosTransactionDto.builder()
            .orderId(105L)
            .amount(new BigDecimal("15.00"))
            .method("CASH")
            .timestamp(Instant.now().minusSeconds(1800))
            .build());

    BigDecimal expectedCash = new BigDecimal("40.50");
    BigDecimal discrepancy = actualCashCounted.subtract(expectedCash);

    return PosReconciliationDto.builder()
        .terminalId(terminalId)
        .cashierId(cashierId)
        .sessionOpenedAt(Instant.now().minusSeconds(28800)) // 8 hours ago
        .sessionClosedAt(Instant.now())
        .expectedCashInDrawer(expectedCash)
        .actualCashCounted(actualCashCounted)
        .discrepancy(discrepancy)
        .totalTransactionCount(2)
        .transactions(transactions)
        .build();
  }
}
