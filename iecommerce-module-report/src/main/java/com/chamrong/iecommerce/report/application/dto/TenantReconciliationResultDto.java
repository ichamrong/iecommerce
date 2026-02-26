package com.chamrong.iecommerce.report.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TenantReconciliationResultDto {
  String tenantId;
  Instant periodStart;
  Instant periodEnd;

  int totalOrdersAnalyzed;
  int totalMismatchesFound;

  BigDecimal sumOfOrders;
  BigDecimal sumOfInvoices;
  BigDecimal sumOfPayments;

  BigDecimal totalDiscrepancyOrdersVsInvoices;
  BigDecimal totalDiscrepancyOrdersVsPayments;

  List<MismatchDetail> mismatches;

  @Value
  @Builder
  public static class MismatchDetail {
    Long orderId;
    String orderCode;
    BigDecimal orderTotal;
    BigDecimal invoiceTotal;
    BigDecimal paymentTotal;
    String status; // e.g. "OVERPAID", "UNDERPAID", "UNINVOICED", etc.
  }
}
