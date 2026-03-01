package com.chamrong.iecommerce.staff.api;

import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.staff.application.command.CreateStaffCommand;
import com.chamrong.iecommerce.staff.application.command.CreateStaffHandler;
import com.chamrong.iecommerce.staff.application.command.ReactivateStaffHandler;
import com.chamrong.iecommerce.staff.application.command.SuspendStaffHandler;
import com.chamrong.iecommerce.staff.application.command.TerminateStaffHandler;
import com.chamrong.iecommerce.staff.application.command.UpdateStaffCommand;
import com.chamrong.iecommerce.staff.application.command.UpdateStaffProfileHandler;
import com.chamrong.iecommerce.staff.application.command.UpdateStaffTenantsCommand;
import com.chamrong.iecommerce.staff.application.command.UpdateStaffTenantsHandler;
import com.chamrong.iecommerce.staff.application.dto.StaffCursorResponse;
import com.chamrong.iecommerce.staff.application.dto.StaffResponse;
import com.chamrong.iecommerce.staff.application.query.StaffQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Platform staff management endpoints.
 *
 * <p>All exceptions are handled by {@link StaffExceptionHandler} — no try/catch needed here.
 */
@Tag(name = "Staff", description = "Platform staff management — requires `staff:manage` permission")
@RestController
@RequestMapping("/api/v1/admin/staff")
@PreAuthorize(Permissions.HAS_STAFF_MANAGE)
@RequiredArgsConstructor
public class StaffController {

  private final CreateStaffHandler createStaffHandler;
  private final UpdateStaffProfileHandler updateProfileHandler;
  private final UpdateStaffTenantsHandler updateTenantsHandler;
  private final StaffQueryHandler staffQueryHandler;
  private final SuspendStaffHandler suspendStaffHandler;
  private final ReactivateStaffHandler reactivateStaffHandler;
  private final TerminateStaffHandler terminateStaffHandler;

  // ── Queries ──────────────────────────────────────────────────────────────

  @Operation(summary = "List staff (cursor pagination)")
  @GetMapping
  public ResponseEntity<StaffCursorResponse<StaffResponse>> listStaff(
      @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "20") int limit) {
    return ResponseEntity.ok(staffQueryHandler.findAll(cursor, Math.min(limit, 100)));
  }

  @Operation(summary = "Get staff by ID")
  @GetMapping("/{id}")
  public ResponseEntity<StaffResponse> getStaff(@PathVariable Long id) {
    String tenantId = TenantContext.requireTenantId();
    return ResponseEntity.ok(staffQueryHandler.findById(tenantId, id));
  }

  // ── Commands ─────────────────────────────────────────────────────────────

  @Operation(summary = "Create staff member")
  @PostMapping
  public ResponseEntity<StaffResponse> createStaff(@Valid @RequestBody CreateStaffCommand cmd) {
    return ResponseEntity.status(HttpStatus.CREATED).body(createStaffHandler.handle(cmd));
  }

  @Operation(summary = "Update staff profile")
  @PutMapping("/{id}")
  public ResponseEntity<StaffResponse> updateProfile(
      @PathVariable Long id, @Valid @RequestBody UpdateStaffCommand cmd) {
    UpdateStaffCommand resolved =
        new UpdateStaffCommand(
            id, cmd.fullName(), cmd.phone(), cmd.department(), cmd.branch(), cmd.role());
    return ResponseEntity.ok(updateProfileHandler.handle(resolved));
  }

  @Operation(summary = "Update staff tenant assignments")
  @PutMapping("/{id}/tenants")
  public ResponseEntity<StaffResponse> updateTenants(
      @PathVariable Long id, @RequestBody Set<String> tenantCodes) {
    return ResponseEntity.ok(
        updateTenantsHandler.handle(new UpdateStaffTenantsCommand(id, tenantCodes)));
  }

  // ── Lifecycle ────────────────────────────────────────────────────────────

  @Operation(summary = "Suspend a staff member")
  @PatchMapping("/{id}/suspend")
  public ResponseEntity<Void> suspendStaff(@PathVariable Long id) {
    suspendStaffHandler.handle(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Reactivate a staff member")
  @PatchMapping("/{id}/reactivate")
  public ResponseEntity<Void> reactivateStaff(@PathVariable Long id) {
    reactivateStaffHandler.handle(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Terminate a staff member")
  @PatchMapping("/{id}/terminate")
  public ResponseEntity<Void> terminateStaff(@PathVariable Long id) {
    terminateStaffHandler.handle(id);
    return ResponseEntity.noContent().build();
  }
}
