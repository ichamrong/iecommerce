package com.chamrong.iecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.IecommerceApplication;
import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.common.AbstractIntegrationTest;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = IecommerceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
public class AuthToCustomerIntegrationTest extends AbstractIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  @DisplayName(
      "Registering an auth user should publish an event and auto-create a Customer profile")
  void testUserRegisteredEventCreatesCustomer() {
    String testUsername = "shopper_bob";
    String testEmail = "bob.shopper@example.com";
    String tenantId = "TENANT_A";

    // 1. Register a new user via Auth endpoint
    ResponseEntity<AuthResponse> registerResp =
        restTemplate.postForEntity(
            "/api/v1/auth/register",
            new RegisterCommand(testUsername, testEmail, "Password123!", tenantId),
            AuthResponse.class);

    assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    AuthResponse authResponse = registerResp.getBody();
    assertThat(authResponse).isNotNull();
    String token = authResponse.accessToken();
    assertThat(token).isNotBlank();

    // 2. Login as admin to fetch all customers and verify the Customer was created via event
    var headers = new HttpHeaders();
    ResponseEntity<AuthResponse> adminLoginResp =
        restTemplate.postForEntity(
            "/api/v1/auth/login",
            new com.chamrong.iecommerce.auth.application.command.LoginCommand(
                "admin", "admin", "SYSTEM"),
            AuthResponse.class);

    assertThat(adminLoginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    String adminToken = adminLoginResp.getBody().accessToken();
    headers.set("Authorization", "Bearer " + adminToken);

    ResponseEntity<List<CustomerResponse>> customersResp =
        restTemplate.exchange(
            "/api/v1/customers",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<CustomerResponse>>() {});

    assertThat(customersResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<CustomerResponse> customers = customersResp.getBody();
    assertThat(customers).isNotNull();

    boolean customerFound = customers.stream().anyMatch(c -> testEmail.equals(c.email()));

    assertThat(customerFound)
        .as("Customer profile must be auto-created after user registration")
        .isTrue();
  }
}
