package com.chamrong.iecommerce.auth.infrastructure.security;

import com.chamrong.iecommerce.auth.domain.Permissions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final TenantContextFilter tenantContextFilter;
  private final CustomAuthenticationEntryPoint authenticationEntryPoint;
  private final CustomAccessDeniedHandler accessDeniedHandler;

  public SecurityConfig(
      TenantContextFilter tenantContextFilter,
      CustomAuthenticationEntryPoint authenticationEntryPoint,
      CustomAccessDeniedHandler accessDeniedHandler) {
    this.tenantContextFilter = tenantContextFilter;
    this.authenticationEntryPoint = authenticationEntryPoint;
    this.accessDeniedHandler = accessDeniedHandler;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .jwt(
                        jwt ->
                            jwt.jwtAuthenticationConverter(
                                new KeycloakJwtAuthenticationConverter()))
                    .authenticationEntryPoint(authenticationEntryPoint))
        // Register TenantContextFilter to run AFTER Spring extracts the Bearer token and sets the
        // SecurityContext
        .addFilterAfter(tenantContextFilter, BearerTokenAuthenticationFilter.class)
        .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public auth endpoints
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/**")
                    .permitAll()
                    // Self-service tenant registration — public
                    .requestMatchers(HttpMethod.POST, "/api/v1/tenants/register")
                    .permitAll()
                    // OpenAPI / Swagger
                    .requestMatchers(
                        "/v3/api-docs/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    // Custom error pages
                    .requestMatchers("/error", "/error/**")
                    .permitAll()
                    // Permission-based rules (defence in depth alongside @PreAuthorize)
                    .requestMatchers(HttpMethod.GET, "/api/v1/users", "/api/v1/users/*")
                    .hasAuthority(Permissions.USER_READ)
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/disable")
                    .hasAuthority(Permissions.USER_DISABLE)
                    .requestMatchers("/api/v1/admin/staff", "/api/v1/admin/staff/**")
                    .hasAuthority(Permissions.STAFF_MANAGE)
                    .requestMatchers(HttpMethod.POST, "/api/v1/admin/tenants")
                    .hasAuthority(Permissions.TENANT_CREATE)
                    .requestMatchers(HttpMethod.POST, "/api/v1/customers")
                    .hasAuthority(Permissions.USER_CREATE)
                    .requestMatchers(HttpMethod.GET, "/api/v1/customers")
                    .hasAuthority(Permissions.USER_READ)
                    .requestMatchers(
                        HttpMethod.GET, "/api/v1/customers/*", "/api/v1/customers/auth/*")
                    .hasAuthority(Permissions.PROFILE_READ)
                    // Deny-by-Default Strategy: Everything else requires a valid JWT
                    .anyRequest()
                    .authenticated())
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    // Maintained for backward compatibility or edge cases, though Keycloak typically handles core
    // hashing
    return new BCryptPasswordEncoder();
  }
}
