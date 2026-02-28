package com.chamrong.iecommerce.auth.api;

import com.chamrong.iecommerce.auth.application.PosAuthDto;
import com.chamrong.iecommerce.auth.application.PosService;
import com.chamrong.iecommerce.auth.domain.PosSession;
import com.chamrong.iecommerce.auth.domain.PosTerminal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "POS Auth API", description = "Terminal & Session Endpoints")
@RestController
@RequestMapping("/api/v1/tenants/me/pos")
@RequiredArgsConstructor
public class PosAuthApiController {

  private final PosService posService;

  @Operation(summary = "Register Terminal")
  @PostMapping("/terminals")
  public ResponseEntity<PosAuthDto.TerminalResponse> registerTerminal(
      @AuthenticationPrincipal Jwt jwt, @RequestBody PosAuthDto.TerminalRegisterRequest req) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    PosTerminal terminal =
        posService.registerTerminal(tenantId, req.name(), req.hardwareId(), req.branchId());
    return ResponseEntity.ok(
        new PosAuthDto.TerminalResponse(
            terminal.getId(),
            terminal.getName(),
            terminal.getHardwareId(),
            terminal.getBranchId(),
            terminal.isActive(),
            terminal.isPendingPairing()));
  }

  @Operation(summary = "List Terminals")
  @GetMapping("/terminals")
  public ResponseEntity<List<PosAuthDto.TerminalResponse>> listTerminals(
      @AuthenticationPrincipal Jwt jwt) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    return ResponseEntity.ok(
        posService.listTerminals(tenantId).stream()
            .map(
                t ->
                    new PosAuthDto.TerminalResponse(
                        t.getId(),
                        t.getName(),
                        t.getHardwareId(),
                        t.getBranchId(),
                        t.isActive(),
                        t.isPendingPairing()))
            .toList());
  }

  @Operation(summary = "Open Session")
  @PostMapping("/sessions")
  public ResponseEntity<PosAuthDto.SessionResponse> openSession(
      @AuthenticationPrincipal Jwt jwt, @RequestBody PosAuthDto.SessionOpenRequest req) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    PosSession session = posService.openSession(tenantId, req.terminalId(), req.cashierId());
    return ResponseEntity.ok(
        new PosAuthDto.SessionResponse(
            session.getId(),
            session.getTerminalId(),
            session.getCashierId(),
            session.getOpenedAt(),
            session.getClosedAt(),
            session.getClosingNotes(),
            session.isActive()));
  }

  @Operation(summary = "Close Session")
  @PatchMapping("/sessions/{sessionId}/close")
  public ResponseEntity<Void> closeSession(
      @PathVariable Long sessionId, @RequestBody PosAuthDto.SessionCloseRequest req) {
    posService.closeSession(sessionId, req.notes());
    return ResponseEntity.noContent().build();
  }
}
