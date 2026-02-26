package com.chamrong.iecommerce.report.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PosReconciliationDto {
  private Long terminalId;
  private Long cashierId;
  private Instant sessionOpenedAt;
  private Instant sessionClosedAt;
  private BigDecimal expectedCashInDrawer;
  private BigDecimal actualCashCounted;
  private BigDecimal discrepancy;
  private int totalTransactionCount;
  private List<PosTransactionDto> transactions;

  @Data
  @Builder
  public static class PosTransactionDto {
    private Long orderId;
    private Instant timestamp;
    private BigDecimal amount;
    private String method; // CASH, CARD, etc.
  }
}
