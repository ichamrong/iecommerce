package com.chamrong.iecommerce.auth.api;

import com.chamrong.iecommerce.auth.application.dto.RoleResponse;
import com.chamrong.iecommerce.auth.application.query.ListRolesHandler;
import com.chamrong.iecommerce.auth.domain.Permissions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only roles API for admin/command center. */
@Tag(name = "Roles", description = "List roles (read-only)")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

  private final ListRolesHandler listRolesHandler;

  @Operation(
      summary = "List all roles",
      description =
          "Returns all roles with permissions. Read-only. Requires `user:read` permission.")
  @GetMapping
  @PreAuthorize(Permissions.HAS_USER_READ)
  public List<RoleResponse> listRoles() {
    return listRolesHandler.handle();
  }
}
