package com.chamrong.iecommerce.sale.api;

import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.sale.application.command.OpenSessionCommand;
import com.chamrong.iecommerce.sale.application.command.OpenShiftCommand;
import com.chamrong.iecommerce.sale.application.dto.SaleSessionResponse;
import com.chamrong.iecommerce.sale.application.dto.ShiftResponse;
import com.chamrong.iecommerce.sale.application.query.SaleQueryService;
import com.chamrong.iecommerce.sale.application.usecase.SaleSessionUseCase;
import com.chamrong.iecommerce.sale.application.usecase.ShiftUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

  private final ShiftUseCase shiftUseCase;
  private final SaleSessionUseCase sessionUseCase;
  private final SaleQueryService queryService;

  @PostMapping("/shifts")
  @PreAuthorize("hasAuthority('" + Permissions.SALE_MANAGE + "')")
  public ResponseEntity<ShiftResponse> openShift(@RequestBody @Valid OpenShiftCommand command) {
    return ResponseEntity.ok(shiftUseCase.openShift(command));
  }

  @GetMapping("/shifts")
  public ResponseEntity<CursorPageResponse<ShiftResponse>> listShifts(
      @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "20") int limit) {
    String tenantId = TenantContext.requireTenantId();
    int clampedLimit = Math.min(100, Math.max(1, limit));
    return ResponseEntity.ok(queryService.listShifts(tenantId, cursor, clampedLimit, Map.of()));
  }

  @PatchMapping("/shifts/{id}/close")
  @PreAuthorize("hasAuthority('" + Permissions.SALE_MANAGE + "')")
  public ResponseEntity<ShiftResponse> closeShift(@PathVariable @NotNull Long id) {
    return ResponseEntity.ok(shiftUseCase.closeShift(id, TenantContext.requireTenantId()));
  }

  @PostMapping("/sessions")
  @PreAuthorize("hasAuthority('" + Permissions.SALE_MANAGE + "')")
  public ResponseEntity<SaleSessionResponse> openSession(
      @RequestBody @Valid OpenSessionCommand command) {
    var session =
        sessionUseCase.openSession(
            command.tenantId(), command.shiftId(), command.terminalId(), command.currency());
    return ResponseEntity.ok(queryService.toSessionResponse(session));
  }

  @GetMapping("/sessions")
  public ResponseEntity<CursorPageResponse<SaleSessionResponse>> listSessions(
      @RequestParam(required = false) String terminalId,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {
    String tenantId = TenantContext.requireTenantId();
    int clampedLimit = Math.min(100, Math.max(1, limit));
    Map<String, Object> filters =
        terminalId != null && !terminalId.isBlank() ? Map.of("terminalId", terminalId) : Map.of();
    return ResponseEntity.ok(
        queryService.listSessions(tenantId, terminalId, cursor, clampedLimit, filters));
  }

  @PatchMapping("/sessions/{id}/initiate-closing")
  @PreAuthorize("hasAuthority('" + Permissions.SALE_MANAGE + "')")
  public ResponseEntity<SaleSessionResponse> initiateClosing(@PathVariable Long id) {
    String tenantId = TenantContext.requireTenantId();
    var session = sessionUseCase.initiateClosing(id, tenantId, "SYSTEM");
    return ResponseEntity.ok(queryService.toSessionResponse(session));
  }

  @PatchMapping("/sessions/{id}/close")
  @PreAuthorize("hasAuthority('" + Permissions.SALE_MANAGE + "')")
  public ResponseEntity<SaleSessionResponse> closeSession(
      @PathVariable Long id, @RequestParam BigDecimal actualCash) {
    String tenantId = TenantContext.requireTenantId();
    var session =
        sessionUseCase.closeSession(
            id, tenantId, "SYSTEM", new com.chamrong.iecommerce.common.Money(actualCash, "USD"));
    return ResponseEntity.ok(queryService.toSessionResponse(session));
  }
}
