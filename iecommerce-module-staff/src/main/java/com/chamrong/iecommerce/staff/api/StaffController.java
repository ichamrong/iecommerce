package com.chamrong.iecommerce.staff.api;

import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.staff.application.command.CreateStaffCommand;
import com.chamrong.iecommerce.staff.application.command.CreateStaffHandler;
import com.chamrong.iecommerce.staff.application.command.UpdateStaffTenantsCommand;
import com.chamrong.iecommerce.staff.application.command.UpdateStaffTenantsHandler;
import com.chamrong.iecommerce.staff.application.dto.StaffResponse;
import com.chamrong.iecommerce.staff.application.query.StaffQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Platform staff management endpoints — all require {@code staff:manage} permission. */
@Tag(name = "Staff", description = "Platform staff management — requires `staff:manage` permission")
@RestController
@RequestMapping("/api/v1/admin/staff")
@PreAuthorize(Permissions.HAS_STAFF_MANAGE)
public class StaffController {

  private final CreateStaffHandler createStaffHandler;
  private final UpdateStaffTenantsHandler updateTenantsHandler;
  private final StaffQueryHandler staffQueryHandler;

  public StaffController(
      CreateStaffHandler createStaffHandler,
      UpdateStaffTenantsHandler updateTenantsHandler,
      StaffQueryHandler staffQueryHandler) {
    this.createStaffHandler = createStaffHandler;
    this.updateTenantsHandler = updateTenantsHandler;
    this.staffQueryHandler = staffQueryHandler;
  }

  /**
   * Create a new platform staff account.
   *
   * <p>POST /api/v1/admin/staff
   */
  @Operation(
      summary = "Create staff member",
      description = "Creates a new platform staff account. Requires `staff:manage` permission.")
  @PostMapping
  public ResponseEntity<?> createStaff(@RequestBody CreateStaffCommand cmd) {
    try {
      StaffResponse response = createStaffHandler.handle(cmd);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
  }

  /**
   * List all platform staff members.
   *
   * <p>GET /api/v1/admin/staff
   */
  @Operation(
      summary = "List all staff",
      description = "Returns a paginated list of all platform staff members.")
  @GetMapping
  public ResponseEntity<Page<StaffResponse>> listStaff(
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(staffQueryHandler.findAll(pageable));
  }

  /**
   * Get a staff member by ID.
   *
   * <p>GET /api/v1/admin/staff/{id}
   */
  @Operation(
      summary = "Get staff by ID",
      description = "Fetch a specific staff member by their ID.")
  @GetMapping("/{id}")
  public ResponseEntity<?> getStaff(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(staffQueryHandler.findById(id));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Replace assigned tenant list for a staff member.
   *
   * <p>PUT /api/v1/admin/staff/{id}/tenants
   *
   * <p>Staff must re-login for the new assignment to be reflected in their JWT.
   */
  @Operation(
      summary = "Update staff tenant assignments",
      description =
          "Replaces the full list of tenants a staff member is assigned to. Staff must re-login for"
              + " changes to take effect.")
  @PutMapping("/{id}/tenants")
  public ResponseEntity<?> updateTenants(
      @PathVariable Long id, @RequestBody Set<String> tenantCodes) {
    try {
      return ResponseEntity.ok(
          updateTenantsHandler.handle(new UpdateStaffTenantsCommand(id, tenantCodes)));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
