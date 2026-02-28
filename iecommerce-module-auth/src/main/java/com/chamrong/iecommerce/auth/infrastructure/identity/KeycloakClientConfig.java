package com.chamrong.iecommerce.auth.infrastructure.identity;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakClientConfig {

  private final KeycloakProperties properties;

  public KeycloakClientConfig(KeycloakProperties properties) {
    this.properties = properties;
  }

  @Bean
  public Keycloak keycloakAdminClient() {
    return KeycloakBuilder.builder()
        .serverUrl(properties.getServerUrl())
        .realm("master")
        .clientId(properties.getAdmin().getClientId())
        .username(properties.getAdmin().getUsername())
        .password(properties.getAdmin().getPassword())
        .build();
  }
}
