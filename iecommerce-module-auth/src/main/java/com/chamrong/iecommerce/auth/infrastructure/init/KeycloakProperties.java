package com.chamrong.iecommerce.auth.infrastructure.init;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.keycloak")
public class KeycloakProperties {
  private String serverUrl;
  private String realm;
  private AdminProperties admin = new AdminProperties();
  private ClientProperties clients = new ClientProperties();

  @Data
  public static class AdminProperties {
    private String username;
    private String password;
    private String clientId;
  }

  @Data
  public static class ClientProperties {
    private String web;
    private String admin;
  }
}
