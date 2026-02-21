package com.chamrong.iecommerce.audit.api;

import com.chamrong.iecommerce.audit.application.AuditService;
import com.chamrong.iecommerce.audit.application.dto.AuditResponse;
import com.chamrong.iecommerce.auth.domain.Permissions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
          "Returns a paginated list of audit logs for the current authenticated user. Requires"
              + " `profile:read` permission.")
  @GetMapping
  @PreAuthorize(Permissions.HAS_PROFILE_READ)
  public Page<AuditResponse> getMyActivity(
      Principal principal, @PageableDefault(size = 20) Pageable pageable) {
    String userId = principal.getName();
    return auditService.findByUserId(userId, pageable);
  }
}
