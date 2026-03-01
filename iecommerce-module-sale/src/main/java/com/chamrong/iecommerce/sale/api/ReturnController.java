package com.chamrong.iecommerce.sale.api;

import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.sale.application.command.CreateReturnCommand;
import com.chamrong.iecommerce.sale.application.dto.SaleReturnResponse;
import com.chamrong.iecommerce.sale.application.query.SaleQueryService;
import com.chamrong.iecommerce.sale.application.usecase.ReturnUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales/returns")
@RequiredArgsConstructor
@PreAuthorize(
    "hasAuthority('"
        + Permissions.SALE_READ
        + "') or hasAuthority('"
        + Permissions.SALE_MANAGE
        + "')")
public class ReturnController {

  private final ReturnUseCase returnUseCase;
  private final SaleQueryService queryService;

  @PostMapping
  @PreAuthorize("hasAuthority('" + Permissions.SALE_MANAGE + "')")
  public ResponseEntity<SaleReturnResponse> createReturn(
      @RequestBody @Valid CreateReturnCommand command) {
    return ResponseEntity.ok(returnUseCase.createReturn(command));
  }

  @GetMapping
  public ResponseEntity<CursorPageResponse<SaleReturnResponse>> listReturns(
      @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "20") int limit) {
    String tenantId = com.chamrong.iecommerce.common.TenantContext.requireTenantId();
    int clampedLimit = Math.min(100, Math.max(1, limit));
    return ResponseEntity.ok(
        queryService.listReturns(tenantId, cursor, clampedLimit, java.util.Map.of()));
  }

  @PatchMapping("/{id}/approve")
  @PreAuthorize("hasAuthority('" + Permissions.SALE_MANAGE + "')")
  public ResponseEntity<SaleReturnResponse> approveReturn(
      @PathVariable Long id, @RequestParam @NotBlank String approverId) {
    String tenantId = com.chamrong.iecommerce.common.TenantContext.requireTenantId();
    return ResponseEntity.ok(returnUseCase.approveReturn(id, tenantId, approverId));
  }

  @PatchMapping("/{id}/complete")
  @PreAuthorize("hasAuthority('" + Permissions.SALE_MANAGE + "')")
  public ResponseEntity<SaleReturnResponse> completeReturn(@PathVariable Long id) {
    String tenantId = com.chamrong.iecommerce.common.TenantContext.requireTenantId();
    return ResponseEntity.ok(returnUseCase.completeReturn(id, tenantId));
  }
}
