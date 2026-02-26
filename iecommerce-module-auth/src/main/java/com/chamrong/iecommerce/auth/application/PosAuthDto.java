package com.chamrong.iecommerce.auth.application;

import java.time.Instant;

public class PosAuthDto {

  public record TerminalRegisterRequest(String name, String hardwareId, String branchId) {}

  public record TerminalResponse(
      Long id,
      String name,
      String hardwareId,
      String branchId,
      boolean active,
      boolean pendingPairing) {}

  public record SessionOpenRequest(Long terminalId, Long cashierId) {}

  public record SessionCloseRequest(String notes) {}

  public record SessionResponse(
      Long id,
      Long terminalId,
      Long cashierId,
      Instant openedAt,
      Instant closedAt,
      String closingNotes,
      boolean active) {}
}
