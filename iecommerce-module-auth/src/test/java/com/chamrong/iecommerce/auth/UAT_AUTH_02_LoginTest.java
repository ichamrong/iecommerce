package com.chamrong.iecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

/** UAT-AUTH-02: A registered user can log in with correct credentials and receives a JWT. */
@SpringBootTest(
    classes = AuthTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UAT_AUTH_02_LoginTest {

  @Autowired private TestRestTemplate restTemplate;

  @BeforeEach
  void registerUser() {
    var cmd = new RegisterCommand("bob", "bob@example.com", "P@ssword1!", "TENANT_A");
    restTemplate.postForEntity("/api/v1/auth/register", cmd, String.class);
  }

  @Test
  @DisplayName("UAT-AUTH-02: Login with correct credentials returns 200 and a JWT token")
  void loginWithCorrectCredentialsReturnsToken() {
    var login = new LoginCommand("bob", "P@ssword1!", "TENANT_A");

    var response = restTemplate.postForEntity("/api/v1/auth/login", login, AuthResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().accessToken()).isNotBlank();
  }

  @Test
  @DisplayName("UAT-AUTH-02b: Login with wrong password returns 401")
  void loginWithWrongPasswordReturnsUnauthorized() {
    var login = new LoginCommand("bob", "wrongPassword", "TENANT_A");

    var response = restTemplate.postForEntity("/api/v1/auth/login", login, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
