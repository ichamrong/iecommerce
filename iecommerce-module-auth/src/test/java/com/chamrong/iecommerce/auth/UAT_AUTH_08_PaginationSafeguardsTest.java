package com.chamrong.iecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.PermissionRepository;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.RoleRepository;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = AuthTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UAT_AUTH_08_PaginationSafeguardsTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private PermissionRepository permissionRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("UAT-AUTH-08: Pagination pageSize is clamped to 100 maximum")
  void pageSizeIsClampedToMaximum() throws Exception {
    // 1. Setup: Create Admin
    createAdminUser("pagination_admin", "TENANT_P");

    // 2. Login
    var loginResp =
        restTemplate.postForEntity(
            "/api/v1/auth/login",
            new LoginCommand("pagination_admin", "Pass123!", "TENANT_P"),
            AuthResponse.class);
    assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    var token = loginResp.getBody().accessToken();

    // 3. Request with massive pageSize
    var headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);

    // Requesting size=10000
    var response =
        restTemplate.exchange(
            "/api/v1/users?size=10000", HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // 4. Verify limit
    JsonNode root = objectMapper.readTree(response.getBody());
    int size = root.path("pageable").path("pageSize").asInt();

    assertThat(size).as("Requested size 10000 should be clamped to 100").isEqualTo(100);
  }

  @Test
  @DisplayName("UAT-AUTH-08: Default page size is 20")
  void defaultPageSizeIsApplied() throws Exception {
    createAdminUser("pagination_admin_default", "TENANT_D");

    var loginResp =
        restTemplate.postForEntity(
            "/api/v1/auth/login",
            new LoginCommand("pagination_admin_default", "Pass123!", "TENANT_D"),
            AuthResponse.class);
    var token = loginResp.getBody().accessToken();

    var headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);

    var response =
        restTemplate.exchange(
            "/api/v1/users", HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode root = objectMapper.readTree(response.getBody());
    int size = root.path("pageable").path("pageSize").asInt();

    assertThat(size).as("Default page size should be 20").isEqualTo(20);
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
    user.setPassword(passwordEncoder.encode("Pass123!"));
    user.setTenantId(tenantId);
    user.setRoles(Set.of(adminRole));
    user.setEnabled(true);
    userRepository.save(user);
  }
}
