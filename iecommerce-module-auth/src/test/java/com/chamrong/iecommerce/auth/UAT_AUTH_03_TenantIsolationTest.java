package com.chamrong.iecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
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

/**
 * UAT-AUTH-03: A valid JWT grants access to authenticated endpoints, but ADMIN-only endpoints
 * enforce role-based access control — non-admin users receive 403 Forbidden.
 */
@SpringBootTest(
    classes = AuthTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UAT_AUTH_03_TenantIsolationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  @DisplayName(
      "UAT-AUTH-03: Valid JWT accepted on auth endpoints; non-ADMIN blocked from user list (403)")
  void jwtGrantsAuthAndEnforcesRoleIsolation() {
    // Register a regular (non-admin) user — unique name to avoid conflicts across test runs
    restTemplate.postForEntity(
        "/api/v1/auth/register",
        new RegisterCommand("uat03_user", "uat03@example.com", "Secret1!", "TENANT_A"),
        String.class);

    // Login → obtain JWT
    var loginResp =
        restTemplate.postForEntity(
            "/api/v1/auth/login",
            new LoginCommand("uat03_user", "Secret1!", "TENANT_A"),
            AuthResponse.class);
    assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResp.getBody()).isNotNull();
    var token = loginResp.getBody().accessToken();
    assertThat(token).isNotBlank();

    // Build Authorization header
    var headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);

    // JWT must be accepted on any authenticated endpoint — a non-existent ID returns 404, not 401
    var getByIdResp =
        restTemplate.exchange(
            "/api/v1/users/99999", HttpMethod.GET, new HttpEntity<>(headers), String.class);
    assertThat(getByIdResp.getStatusCode())
        .as("Valid JWT must be accepted (not 401 Unauthorized)")
        .isNotEqualTo(HttpStatus.UNAUTHORIZED);

    // Non-admin user MUST be denied the ADMIN-only list endpoint → 403 Forbidden
    var listResp =
        restTemplate.exchange(
            "/api/v1/users", HttpMethod.GET, new HttpEntity<>(headers), String.class);
    assertThat(listResp.getStatusCode())
        .as("Non-admin JWT should yield 403 Forbidden, not 401")
        .isEqualTo(HttpStatus.FORBIDDEN);
  }
}
