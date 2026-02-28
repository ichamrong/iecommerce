package com.chamrong.iecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.PermissionRepository;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.RoleRepository;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

/** UAT-AUTH-03 (Strict): Verify that an Admin from Tenant A cannot see users from Tenant B. */
@SpringBootTest(
    classes = AuthTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UAT_AUTH_07_TenantIsolationStrictTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private PermissionRepository permissionRepository;

  @Test
  @DisplayName("UAT-AUTH-03: Admin A should NOT see users from Tenant B")
  void adminFromTenantACannotSeeUsersFromTenantB() {
    // 1. Setup: Create Admin A in Tenant A (via DB to ensure ROLE_ADMIN)
    createAdminUser("admin_a", "TENANT_A");

    // 2. Setup: Create User B in Tenant B (via API or DB)
    restTemplate.postForEntity(
        "/api/v1/auth/register",
        new RegisterCommand("user_b", "user_b@example.com", "Pass123!", "TENANT_B"),
        String.class);

    // 3. Login as Admin A
    var loginResp =
        restTemplate.postForEntity(
            "/api/v1/auth/login",
            new LoginCommand("admin_a", "Pass123!", "TENANT_A"),
            AuthResponse.class);
    assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    var token = loginResp.getBody().accessToken();

    // 4. Request /api/v1/users
    var headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    var response =
        restTemplate.exchange(
            "/api/v1/users", HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // 5. Verify Response contains Admin A but NOT User B
    String body = response.getBody();
    assertThat(body).contains("admin_a");
    assertThat(body).doesNotContain("user_b");
  }

  private void createAdminUser(String username, String tenantId) {
    if (userRepository.findByUsernameAndTenantId(username, tenantId).isPresent()) return;

    Permission userRead =
        permissionRepository
            .findByName(Permissions.USER_READ)
            .orElseGet(() -> permissionRepository.save(new Permission(Permissions.USER_READ)));

    Role adminRole =
        roleRepository
            .findByName("ROLE_ADMIN")
            .orElseGet(
                () -> {
                  Role r = new Role("ROLE_ADMIN");
                  r.setTenantId("SYSTEM");
                  return r;
                });
    adminRole.setPermissions(Set.of(userRead));
    adminRole = roleRepository.save(adminRole);

    User user = new User();
    user.setUsername(username);
    user.setEmail(username + "@test.com");
    user.setTenantId(tenantId);
    user.setRoles(Set.of(adminRole));
    user.setEnabled(true);
    userRepository.save(user);
  }
}
