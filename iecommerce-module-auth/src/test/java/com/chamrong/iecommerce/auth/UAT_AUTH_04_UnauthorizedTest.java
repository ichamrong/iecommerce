package com.chamrong.iecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

/** UAT-AUTH-04: Any request to a protected endpoint without a valid JWT must return 401. */
@SpringBootTest(
    classes = AuthTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UAT_AUTH_04_UnauthorizedTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  @DisplayName("UAT-AUTH-04: GET /api/v1/users without token returns 401")
  void accessWithoutTokenReturnsUnauthorized() {
    var response = restTemplate.getForEntity("/api/v1/users", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("UAT-AUTH-04b: GET /api/v1/users/{id} without token returns 401")
  void getByIdWithoutTokenReturnsUnauthorized() {
    var response = restTemplate.getForEntity("/api/v1/users/1", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
