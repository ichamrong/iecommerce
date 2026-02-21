package com.chamrong.iecommerce.audit.api;

import com.chamrong.iecommerce.audit.application.AuditService;
import com.chamrong.iecommerce.audit.application.dto.AuditQuery;
import com.chamrong.iecommerce.audit.application.dto.AuditResponse;
import com.chamrong.iecommerce.auth.domain.Permissions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin — Audit", description = "User activity logs management")
@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
public class AuditController {

  private final AuditService auditService;

  @Operation(
      summary = "List all audit logs",
      description =
          "Returns a paginated list of all system audit logs with optional filtering and keyword"
              + " search. Requires `audit:read` permission.")
  @GetMapping
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public Page<AuditResponse> listAll(
      @RequestParam(required = false) String userId,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) String resourceType,
      @RequestParam(required = false) String resourceId,
      @RequestParam(required = false) String searchTerm,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @PageableDefault(size = 20) Pageable pageable) {
    var query = new AuditQuery(userId, action, resourceType, resourceId, searchTerm, from, to);
    return auditService.findByQuery(query, pageable);
  }

  @Operation(
      summary = "Get audit log by ID",
      description =
          "Returns the full details of a single audit log entry. Requires `audit:read` permission.")
  @GetMapping("/{id}")
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public ResponseEntity<AuditResponse> getById(@PathVariable Long id) {
    return auditService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "Get resource audit history",
      description =
          "Returns a paginated list of audit logs for a specific resource. Requires `audit:read`"
              + " permission.")
  @GetMapping("/resource/{resourceType}/{resourceId}")
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public Page<AuditResponse> getResourceHistory(
      @PathVariable String resourceType,
      @PathVariable String resourceId,
      @PageableDefault(size = 20) Pageable pageable) {
    var query = new AuditQuery(null, null, resourceType, resourceId, null, null, null);
    return auditService.findByQuery(query, pageable);
  }

  @Operation(
      summary = "List unique actions",
      description = "Returns a list of all unique audit actions recorded in the system.")
  @GetMapping("/actions")
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public List<String> getActions() {
    return auditService.getUniqueActions();
  }

  @Operation(
      summary = "List unique resource types",
      description = "Returns a list of all unique resource types recorded in the system.")
  @GetMapping("/resource-types")
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public List<String> getResourceTypes() {
    return auditService.getUniqueResourceTypes();
  }

  @Operation(
      summary = "List audit logs by user",
      description =
          "Returns a paginated list of audit logs for a specific user. Requires `audit:read`"
              + " permission.")
  @GetMapping("/user/{userId}")
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public Page<AuditResponse> listByUser(
      @PathVariable String userId, @PageableDefault(size = 20) Pageable pageable) {
    return auditService.findByUserId(userId, pageable);
  }
}
