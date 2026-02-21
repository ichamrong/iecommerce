package com.chamrong.iecommerce.auth.infrastructure.init;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class KongInitializer implements CommandLineRunner {

  private final KongProperties properties;
  private final RestClient restClient = RestClient.builder().build();

  @Override
  public void run(String... args) {
    log.info("Starting Kong API Gateway Auto-Configuration at {}", properties.getAdminUrl());

    try {
      ensureServiceExists();
      ensureRoutesExist();
      ensureRateLimitingPlugin();
      ensureJwtValidationPlugin();

      log.info("Kong Auto-Configuration Complete.");
    } catch (Exception e) {
      log.error("Failed to initialize Kong API Gateway. Is it running? Error: {}", e.getMessage());
    }
  }

  private void ensureServiceExists() {
    String serviceName = "iecommerce-backend";
    try {
      restClient
          .get()
          .uri(properties.getAdminUrl() + "/services/" + serviceName)
          .retrieve()
          .toBodilessEntity();
      log.info("Kong Service '{}' already exists.", serviceName);
    } catch (HttpClientErrorException.NotFound e) {
      log.info(
          "Creating Kong Service '{}' pointing to {}", serviceName, properties.getUpstreamUrl());
      restClient
          .post()
          .uri(properties.getAdminUrl() + "/services")
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("name", serviceName, "url", properties.getUpstreamUrl()))
          .retrieve()
          .toBodilessEntity();
      log.info("Kong Service '{}' created.", serviceName);
    }
  }

  private void ensureRoutesExist() {
    String routeName = "iecommerce-api-route";
    String serviceName = "iecommerce-backend";

    try {
      restClient
          .get()
          .uri(properties.getAdminUrl() + "/routes/" + routeName)
          .retrieve()
          .toBodilessEntity();
      log.info("Kong Route '{}' already exists.", routeName);
    } catch (HttpClientErrorException.NotFound e) {
      log.info("Creating Kong Route '{}'...", routeName);
      restClient
          .post()
          .uri(properties.getAdminUrl() + "/services/" + serviceName + "/routes")
          .contentType(MediaType.APPLICATION_JSON)
          .body(
              Map.of(
                  "name",
                  routeName,
                  "paths",
                  List.of(
                      "/api/v1/auth",
                      "/api/v1/admin",
                      "/api/v1/customers",
                      "/api/v1/tenants",
                      "/api/v1/storefront")))
          .retrieve()
          .toBodilessEntity();
      log.info("Kong Route '{}' created.", routeName);
    }
  }

  private void ensureRateLimitingPlugin() {
    ensurePlugin(
        "rate-limiting",
        Map.of("second", 10, "policy", "redis", "redis_host", "redis", "redis_port", 6379));
  }

  private void ensureJwtValidationPlugin() {
    // Here we configure the edge-level OIDC JWT validation pointing to the Keycloak Realm
    ensurePlugin(
        "jwt-keycloak",
        Map.of(
            "allowed_issuers",
            List.of(
                "http://localhost:8080/realms/iecommerce",
                "http://keycloak:8080/realms/iecommerce"),
            "well_known_template",
            "http://keycloak:8080/realms/iecommerce/.well-known/openid-configuration"));
  }

  private void ensurePlugin(String pluginName, Map<String, Object> config) {
    String serviceName = "iecommerce-backend";
    try {
      // In a real environment, we'd check if the plugin is already attached to this specific
      // service.
      // For simplicity, we wrap the POST in a try/catch, ignoring 409 Conflict.
      restClient
          .post()
          .uri(properties.getAdminUrl() + "/services/" + serviceName + "/plugins")
          .contentType(MediaType.APPLICATION_JSON)
          .body(
              Map.of(
                  "name", pluginName,
                  "config", config))
          .retrieve()
          .toBodilessEntity();
      log.info("Kong Plugin '{}' enabled on service.", pluginName);
    } catch (HttpClientErrorException.Conflict e) {
      log.info("Kong Plugin '{}' is already enabled.", pluginName);
    } catch (Exception e) {
      // Note: If the open-source Kong image does not have the 'jwt-keycloak' plugin installed, this
      // will throw 400 Bad Request
      log.warn(
          "Could not enable Kong plugin '{}'. If using standard Kong, ensure the custom plugin is"
              + " installed. Reason: {}",
          pluginName,
          e.getMessage());
    }
  }
}
