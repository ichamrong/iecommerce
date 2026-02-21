package com.chamrong.iecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

/**
 * UAT-AUTH-05: Attempting to register a user with an already-taken username or email returns 409.
 */
@SpringBootTest(
    classes = AuthTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UAT_AUTH_05_DuplicateRegisterTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  @DisplayName("UAT-AUTH-05: Duplicate username registration returns 409 Conflict")
  void duplicateUsernameReturnsConflict() {
    var cmd = new RegisterCommand("carol", "carol@example.com", "P@ssword1!", "TENANT_A");

    // First registration — should succeed
    var first = restTemplate.postForEntity("/api/v1/auth/register", cmd, AuthResponse.class);
    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // Second registration with same username — should fail
    var duplicate = new RegisterCommand("carol", "carol2@example.com", "P@ssword1!", "TENANT_A");
    var second = restTemplate.postForEntity("/api/v1/auth/register", duplicate, String.class);
    assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  @DisplayName("UAT-AUTH-05b: Duplicate email registration returns 409 Conflict")
  void duplicateEmailReturnsConflict() {
    var cmd = new RegisterCommand("dave", "dave@example.com", "P@ssword1!", "TENANT_A");
    restTemplate.postForEntity("/api/v1/auth/register", cmd, AuthResponse.class);

    var duplicate = new RegisterCommand("dave2", "dave@example.com", "P@ssword1!", "TENANT_A");
    var second = restTemplate.postForEntity("/api/v1/auth/register", duplicate, String.class);
    assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }
}
