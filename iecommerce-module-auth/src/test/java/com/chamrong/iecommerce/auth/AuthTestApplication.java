package com.chamrong.iecommerce.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.command.auth.LoginUserHandler;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.infrastructure.ratelimit.RateLimitProperties;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/** Minimal Spring Boot application for auth module slice tests. */
@SpringBootApplication
@EntityScan(basePackages = "com.chamrong.iecommerce.auth.domain")
@EnableJpaRepositories(basePackages = "com.chamrong.iecommerce.auth.infrastructure.persistence")
@EnableJpaAuditing
@EnableMethodSecurity
@EnableConfigurationProperties(RateLimitProperties.class)
public class AuthTestApplication {
  @Bean
  @Primary
  public JwtDecoder jwtDecoder() {
    JwtDecoder mock = mock(JwtDecoder.class);

    when(mock.decode(anyString()))
        .thenAnswer(
            invocation -> {
              String token = invocation.getArgument(0);

              String tenantId = "TENANT_A";
              if (token.contains(".")) {
                tenantId = token.split("\\.")[0];
              }

              List<String> roles = new java.util.ArrayList<>(List.of("ROLE_CUSTOMER"));
              if (token.toLowerCase().contains("admin")) {
                roles.add("ROLE_ADMIN");
                roles.add("user:read");
                roles.add("user:create");
                roles.add("tenant:create");
                roles.add("staff:manage");
              } else {
                roles.add("profile:read");
              }

              Instant now = Instant.now();
              return Jwt.withTokenValue(token)
                  .header("alg", "none")
                  .header("typ", "JWT")
                  .subject("fake-sub")
                  .issuer("http://localhost/realms/iecommerce")
                  .issuedAt(now.minusSeconds(60))
                  .notBefore(now.minusSeconds(60))
                  .expiresAt(now.plusSeconds(3600))
                  .claim("preferred_username", "bob")
                  .claim("tenantId", tenantId)
                  .claim("realm_access", Map.of("roles", roles))
                  .build();
            });
    return mock;
  }

  @Bean
  @Primary
  public Keycloak keycloak() {
    Keycloak keycloak = mock(Keycloak.class);
    RealmsResource realmsResource = mock(RealmsResource.class);
    RealmResource realmResource = mock(RealmResource.class);
    UsersResource usersResource = mock(UsersResource.class);
    RolesResource rolesResource = mock(RolesResource.class);
    org.keycloak.admin.client.resource.ClientsResource clientsResource =
        mock(org.keycloak.admin.client.resource.ClientsResource.class);
    org.keycloak.admin.client.resource.UserResource userResource =
        mock(org.keycloak.admin.client.resource.UserResource.class);
    org.keycloak.admin.client.resource.RoleMappingResource roleMappingResource =
        mock(org.keycloak.admin.client.resource.RoleMappingResource.class);
    org.keycloak.admin.client.resource.RoleScopeResource roleScopeResource =
        mock(org.keycloak.admin.client.resource.RoleScopeResource.class);
    org.keycloak.admin.client.resource.RoleResource roleResource =
        mock(org.keycloak.admin.client.resource.RoleResource.class);

    when(keycloak.realms()).thenReturn(realmsResource);
    when(realmsResource.findAll()).thenReturn(java.util.Collections.emptyList());
    when(keycloak.realm(anyString())).thenReturn(realmResource);
    when(realmResource.users()).thenReturn(usersResource);
    when(realmResource.roles()).thenReturn(rolesResource);
    when(realmResource.clients()).thenReturn(clientsResource);

    when(clientsResource.findAll()).thenReturn(java.util.Collections.emptyList());
    when(rolesResource.list()).thenReturn(java.util.Collections.emptyList());
    when(rolesResource.get(anyString())).thenReturn(roleResource);

    org.keycloak.representations.idm.RoleRepresentation adminRoleRep =
        new org.keycloak.representations.idm.RoleRepresentation();
    adminRoleRep.setName("ROLE_PLATFORM_ADMIN");
    when(roleResource.toRepresentation()).thenReturn(adminRoleRep);

    when(usersResource.get(anyString())).thenReturn(userResource);
    when(usersResource.searchByUsername(anyString(), any(Boolean.class)))
        .thenReturn(java.util.Collections.emptyList());
    when(userResource.roles()).thenReturn(roleMappingResource);
    // roleScopeResource.add() is void, no need to stub Mockito mock
    when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

    // Mock user creation response with unique ID generation
    when(usersResource.create(any()))
        .thenAnswer(
            invocation -> {
              jakarta.ws.rs.core.Response resp = mock(jakarta.ws.rs.core.Response.class);
              when(resp.getStatus()).thenReturn(201);
              String uniqueId = java.util.UUID.randomUUID().toString();
              when(resp.getLocation())
                  .thenReturn(java.net.URI.create("http://localhost/users/" + uniqueId));
              return resp;
            });

    return keycloak;
  }

  @Bean
  @Primary
  public LoginUserHandler loginUserHandler() {
    LoginUserHandler mock = mock(LoginUserHandler.class);
    when(mock.handle(any()))
        .thenAnswer(
            invocation -> {
              com.chamrong.iecommerce.auth.application.command.LoginCommand cmd =
                  invocation.getArgument(0);

              if (cmd.password() != null && cmd.password().toLowerCase().contains("wrong")) {
                throw new org.springframework.security.authentication.BadCredentialsException(
                    "Invalid credentials");
              }

              String tokenPrefix = cmd.tenantId() + ".";
              String token =
                  tokenPrefix
                      + (cmd.username().toLowerCase().contains("admin")
                          ? "admin-token"
                          : "user-token");

              return new AuthResponse(token, "fake-refresh", 3600, "Bearer", "fake-session");
            });
    return mock;
  }

  @Bean
  @Primary
  public com.chamrong.iecommerce.auth.domain.TenantRepository tenantRepository() {
    com.chamrong.iecommerce.auth.domain.TenantRepository mock =
        mock(com.chamrong.iecommerce.auth.domain.TenantRepository.class);
    when(mock.findByCode(anyString()))
        .thenAnswer(
            invocation -> {
              String code = invocation.getArgument(0);
              com.chamrong.iecommerce.auth.domain.Tenant t =
                  new com.chamrong.iecommerce.auth.domain.Tenant();
              t.setCode(code);
              t.setName("Mock Tenant " + code);
              t.setStatus(com.chamrong.iecommerce.auth.domain.TenantStatus.ACTIVE);
              t.setEnabled(true);
              return java.util.Optional.of(t);
            });
    return mock;
  }

  @Bean
  public org.springframework.boot.CommandLineRunner startupReport() {
    return args -> System.out.println("DEBUG: AuthTestApplication STARTED");
  }
}
