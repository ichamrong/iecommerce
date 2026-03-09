package com.chamrong.iecommerce.ekyc.api;

import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.ekyc.application.EkycApprovalService;
import com.chamrong.iecommerce.ekyc.application.dto.ApprovalListResponse;
import com.chamrong.iecommerce.ekyc.application.dto.ApprovalResponse;
import com.chamrong.iecommerce.ekyc.application.dto.ReviewRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST API for eKYC / merchant approvals. */
@Tag(name = "eKYC", description = "Merchant approval (eKYC) management")
@RestController
@RequestMapping("/api/v1/ekyc/approvals")
@RequiredArgsConstructor
public class EkycApprovalController {

  private final EkycApprovalService service;

  @Operation(
      summary = "List approvals",
      description = "Returns paginated list with optional status and riskScore filters.")
  @GetMapping
  @PreAuthorize(Permissions.HAS_EKYC_READ)
  public ApprovalListResponse list(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String riskScore,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize) {
    return service.list(status, riskScore, page, Math.min(100, Math.max(1, pageSize)));
  }

  @Operation(summary = "Get approval by id")
  @GetMapping("/{id}")
  @PreAuthorize(Permissions.HAS_EKYC_READ)
  public ResponseEntity<ApprovalResponse> getById(@PathVariable String id) {
    return service.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "Submit review decision",
      description = "Approve or reject a pending approval.")
  @PostMapping("/{id}/review")
  @PreAuthorize(Permissions.HAS_EKYC_REVIEW)
  public ResponseEntity<Void> review(
      @PathVariable String id, @Valid @RequestBody ReviewRequest request) {
    try {
      service.review(id, request);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
