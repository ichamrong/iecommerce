package com.chamrong.iecommerce.auth.application.query;

import com.chamrong.iecommerce.auth.application.dto.RoleResponse;
import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.ports.RoleRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Returns all roles for read-only listing (GET /api/v1/roles). */
@Component
@RequiredArgsConstructor
public class ListRolesHandler {

  private final RoleRepositoryPort roleRepository;

  @Transactional(readOnly = true)
  public List<RoleResponse> handle() {
    return roleRepository.findAll().stream().map(this::toResponse).toList();
  }

  private RoleResponse toResponse(Role r) {
    List<String> permissionNames =
        r.getPermissions() == null
            ? List.of()
            : r.getPermissions().stream().map(Permission::getName).toList();
    String tenantId = r.getTenantId() != null ? r.getTenantId() : "SYSTEM";
    return new RoleResponse(
        String.valueOf(r.getId()),
        r.getName(),
        r.getDescription(),
        permissionNames,
        tenantId,
        r.getCreatedAt() != null ? r.getCreatedAt() : java.time.Instant.EPOCH);
  }
}
