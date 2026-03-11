package com.chamrong.iecommerce.auth.application.query;

import com.chamrong.iecommerce.auth.application.dto.MeResponse;
import com.chamrong.iecommerce.auth.application.dto.MeResponse.MeUser;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns the current authenticated user's profile for session restore (e.g. when the frontend
 * loads with httpOnly cookies and no in-memory state).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetMeHandler {

  private static final String SYSTEM_TENANT = "SYSTEM";

  private final UserRepositoryPort userRepository;
  private final IdentityService identityService;

  /**
   * Builds the current user response from SecurityContext (JWT in cookie or header) and local user
   * record.
   */
  @Transactional(readOnly = true)
  public MeResponse handle() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new org.springframework.security.authentication.BadCredentialsException(
          "Not authenticated");
    }

    String username = auth.getName();
    String tenantId = TenantContext.getCurrentTenant();
    if (tenantId == null || tenantId.isBlank()) {
      tenantId = SYSTEM_TENANT;
    }

    User user =
        userRepository
            .findByUsernameAndTenantId(username, tenantId)
            .or(() -> userRepository.findByUsernameAndTenantId(username, SYSTEM_TENANT))
            .or(() -> userRepository.findByUsername(username))
            .orElseThrow(
                () ->
                    new org.springframework.security.authentication.BadCredentialsException(
                        "User not found: " + username));

    List<String> permissions =
        streamRoles(user)
            .flatMap(r -> streamPermissions(r))
            .map(p -> p.getName())
            .distinct()
            .collect(Collectors.toList());

    String primaryRole =
        streamRoles(user)
            .map(r -> r.getName())
            .filter(name -> "ROLE_PLATFORM_ADMIN".equals(name))
            .findFirst()
            .orElse(
                streamRoles(user).map(r -> r.getName()).findFirst().orElse("ROLE_PLATFORM_STAFF"));

    boolean requiresPasswordChange = requiresPasswordChangeSafe(username);

    MeUser meUser =
        new MeUser(
            user.getKeycloakId() != null ? user.getKeycloakId() : String.valueOf(user.getId()),
            user.getEmail(),
            user.getUsername(),
            primaryRole,
            permissions,
            user.getTenantId(),
            user.getKeycloakId() != null ? user.getKeycloakId() : "session",
            null);

    return new MeResponse(meUser, requiresPasswordChange);
  }

  private static Stream<Role> streamRoles(User user) {
    return user.getRoles() != null ? user.getRoles().stream() : Stream.empty();
  }

  private static Stream<Permission> streamPermissions(Role role) {
    return role.getPermissions() != null ? role.getPermissions().stream() : Stream.empty();
  }

  /**
   * Calls IdentityService.requiresPasswordChange; returns false on any exception (e.g. Keycloak
   * unreachable) so GET /me does not return 500.
   */
  private boolean requiresPasswordChangeSafe(String username) {
    try {
      return identityService.requiresPasswordChange(username);
    } catch (Exception e) {
      log.debug(
          "Could not determine required password change for {}: {}", username, e.getMessage());
      return false;
    }
  }
}
