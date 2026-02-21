package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.UserLoggedInEvent;
import com.chamrong.iecommerce.auth.UserLoginFailedEvent;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.infrastructure.init.KeycloakProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginUserHandler {

  private final KeycloakProperties properties;
  private final ApplicationEventPublisher eventPublisher;
  private final RestClient restClient = RestClient.builder().build();

  /**
   * Authenticate a user by proxying the request directly to Keycloak's OAuth2 Token endpoint.
   *
   * @throws BadCredentialsException if username not found or password does not match inside
   *     Keycloak
   */
  public AuthResponse handle(LoginCommand cmd) {
    String tokenUrl =
        properties.getServerUrl()
            + "/realms/"
            + properties.getRealm()
            + "/protocol/openid-connect/token";

    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("client_id", properties.getClients().getWeb());
    formData.add("grant_type", "password");
    formData.add("username", cmd.username());
    formData.add("password", cmd.password());

    try {
      var response =
          restClient
              .post()
              .uri(tokenUrl)
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .body(formData)
              .retrieve()
              .body(AuthResponse.class);

      eventPublisher.publishEvent(new UserLoggedInEvent(cmd.username(), cmd.tenantId()));
      return response;
    } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest e) {
      log.warn("Failed Keycloak login for user: {}", cmd.username());
      eventPublisher.publishEvent(
          new UserLoginFailedEvent(cmd.username(), cmd.tenantId(), "Invalid credentials"));
      throw new BadCredentialsException("Invalid credentials or user not found.");
    }
  }
}
