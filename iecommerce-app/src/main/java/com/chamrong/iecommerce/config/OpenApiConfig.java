package com.chamrong.iecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String BEARER_AUTH = "bearerAuth";

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("IECommerce Platform API")
                .description(
                    """
Multi-tenant e-commerce platform API.

## Authentication
Most endpoints require a **Bearer JWT token** issued by Keycloak.
Use `POST /api/v1/auth/login` to obtain a token, then click **Authorize** above.

## Permission Model
Endpoints are protected by fine-grained permissions (e.g. `user:read`, `staff:manage`).
The super-admin role (`ROLE_PLATFORM_ADMIN`) has full access.
""")
                .version("v1.0.0")
                .contact(new Contact().name("Chamrong").email("admin@platform.com"))
                .license(new License().name("Proprietary").url("https://chamrong.me")))
        .servers(
            List.of(new Server().url("http://localhost:8081").description("Local Development")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_AUTH,
                    new SecurityScheme()
                        .name(BEARER_AUTH)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "JWT token from Keycloak. Use POST /api/v1/auth/login to get one.")));
  }

  /** 🔐 Authentication & Registration */
  @Bean
  public GroupedOpenApi authGroup() {
    return GroupedOpenApi.builder()
        .group("1. Authentication")
        .displayName("🔐 Authentication")
        .pathsToMatch("/api/v1/auth/**")
        .build();
  }

  /** 🏢 Tenant Management */
  @Bean
  public GroupedOpenApi tenantGroup() {
    return GroupedOpenApi.builder()
        .group("2. Tenants")
        .displayName("🏢 Tenant Management")
        .pathsToMatch("/api/v1/tenants/**", "/api/v1/admin/tenants/**", "/api/v1/storefront/**")
        .build();
  }

  /** 👤 User Management */
  @Bean
  public GroupedOpenApi userGroup() {
    return GroupedOpenApi.builder()
        .group("3. Users")
        .displayName("👤 User Management")
        .pathsToMatch("/api/v1/users/**")
        .build();
  }

  /** 🧑‍💼 Staff Management */
  @Bean
  public GroupedOpenApi staffGroup() {
    return GroupedOpenApi.builder()
        .group("4. Staff")
        .displayName("🧑‍💼 Staff Management")
        .pathsToMatch("/api/v1/admin/staff/**")
        .build();
  }

  /** 🛒 Customers */
  @Bean
  public GroupedOpenApi customerGroup() {
    return GroupedOpenApi.builder()
        .group("5. Customers")
        .displayName("🛒 Customers")
        .pathsToMatch("/api/v1/customers/**")
        .build();
  }

  /** 📦 Catalog & Products */
  @Bean
  public GroupedOpenApi catalogGroup() {
    return GroupedOpenApi.builder()
        .group("6. Catalog")
        .displayName("📦 Catalog & Products")
        .pathsToMatch("/api/products/**")
        .build();
  }

  /** 🌐 All APIs */
  @Bean
  public GroupedOpenApi allGroup() {
    return GroupedOpenApi.builder()
        .group("0. All")
        .displayName("🌐 All APIs")
        .pathsToMatch("/api/**")
        .build();
  }
}
