package com.chamrong.iecommerce.audit.api;

import com.chamrong.iecommerce.audit.application.AuditService;
import com.chamrong.iecommerce.audit.application.dto.AuditQuery;
import com.chamrong.iecommerce.audit.application.dto.AuditResponse;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
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
          "Returns a cursor-paginated list of all system audit logs with optional filtering. "
              + "Requires `audit:read` permission.")
  @GetMapping
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public CursorPageResponse<AuditResponse> listAll(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(required = false) String userId,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) String resourceType,
      @RequestParam(required = false) String resourceId,
      @RequestParam(required = false) String searchTerm,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to) {
    var query = new AuditQuery(userId, action, resourceType, resourceId, searchTerm, from, to);
    Map<String, Object> filters = new LinkedHashMap<>();
    if (StringUtils.hasText(userId)) {
      filters.put("userId", userId);
    }
    if (StringUtils.hasText(action)) {
      filters.put("action", action);
    }
    if (StringUtils.hasText(resourceType)) {
      filters.put("resourceType", resourceType);
    }
    if (StringUtils.hasText(resourceId)) {
      filters.put("resourceId", resourceId);
    }
    if (StringUtils.hasText(searchTerm)) {
      filters.put("searchTerm", searchTerm);
    }
    if (from != null) filters.put("from", from);
    if (to != null) filters.put("to", to);
    return auditService.findPageByQuery(
        tenantId,
        query,
        cursor,
        Math.min(100, Math.max(1, limit)),
        AuditService.ENDPOINT_LIST_ALL,
        filters);
  }

  @Operation(
      summary = "Get audit log by ID",
      description =
          "Returns the full details of a single audit log entry. Requires `audit:read` permission.")
  @GetMapping("/{id}")
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public ResponseEntity<AuditResponse> getById(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable Long id) {
    return auditService
        .findById(tenantId, id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "Get resource audit history",
      description =
          "Returns a cursor-paginated list of audit logs for a specific resource. Requires"
              + " `audit:read` permission.")
  @GetMapping("/resource/{resourceType}/{resourceId}")
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public CursorPageResponse<AuditResponse> getResourceHistory(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String resourceType,
      @PathVariable String resourceId,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {
    var query = new AuditQuery(null, null, resourceType, resourceId, null, null, null);
    Map<String, Object> filters = Map.of("resourceType", resourceType, "resourceId", resourceId);
    return auditService.findPageByQuery(
        tenantId,
        query,
        cursor,
        Math.min(100, Math.max(1, limit)),
        AuditService.ENDPOINT_RESOURCE_HISTORY,
        filters);
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
          "Returns a cursor-paginated list of audit logs for a specific user. Requires"
              + " `audit:read` permission.")
  @GetMapping("/user/{userId}")
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public CursorPageResponse<AuditResponse> listByUser(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String userId,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {
    Map<String, Object> filters = Map.of("userId", userId);
    return auditService.findPageByUserId(
        tenantId, userId, cursor, Math.min(100, Math.max(1, limit)), filters);
  }

  @Operation(
      summary = "Export audit logs as CSV",
      description =
          "Returns audit logs matching the same filters as list, as a CSV file. Capped at "
              + AuditService.EXPORT_CSV_MAX_ROWS
              + " rows. Requires `audit:read` permission.")
  @GetMapping("/export")
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public ResponseEntity<String> exportCsv(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestParam(required = false) String userId,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) String resourceType,
      @RequestParam(required = false) String resourceId,
      @RequestParam(required = false) String searchTerm,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to) {
    var query = new AuditQuery(userId, action, resourceType, resourceId, searchTerm, from, to);
    String csv = auditService.exportAsCsv(tenantId, query);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
    headers.setContentDispositionFormData("attachment", "audit-export.csv");
    return ResponseEntity.ok().headers(headers).body(csv);
  }
}
