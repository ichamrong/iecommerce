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

/** UAT-AUTH-01: A new user can register with valid credentials and receives a JWT in return. */
@SpringBootTest(
    classes = AuthTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UAT_AUTH_01_RegisterTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  @DisplayName("UAT-AUTH-01: Successful registration returns 201 and a JWT token")
  void successfulRegistrationReturns201AndToken() {
    var cmd = new RegisterCommand("alice", "alice@example.com", "P@ssword1!", "TENANT_A");

    var response = restTemplate.postForEntity("/api/v1/auth/register", cmd, AuthResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().accessToken()).isNotBlank();
  }
}
