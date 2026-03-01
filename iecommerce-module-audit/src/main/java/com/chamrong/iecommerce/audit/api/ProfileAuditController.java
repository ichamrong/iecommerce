package com.chamrong.iecommerce.audit.api;

import com.chamrong.iecommerce.audit.application.AuditService;
import com.chamrong.iecommerce.audit.application.dto.AuditResponse;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profile — Audit", description = "Self-service activity logs for the current user")
@RestController
@RequestMapping("/api/v1/profile/audit")
@RequiredArgsConstructor
public class ProfileAuditController {

  private final AuditService auditService;

  @Operation(
      summary = "List my activity",
      description =
          "Returns a cursor-paginated list of audit logs for the current authenticated user."
              + " Requires `profile:read` permission.")
  @GetMapping
  @PreAuthorize(Permissions.HAS_PROFILE_READ)
  public CursorPageResponse<AuditResponse> getMyActivity(
      @RequestHeader("X-Tenant-ID") String tenantId,
      Principal principal,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {
    String userId = principal.getName();
    Map<String, Object> filters = Map.of("userId", userId);
    return auditService.findPageByUserId(
        tenantId, userId, cursor, Math.min(100, Math.max(1, limit)), filters);
  }
}
