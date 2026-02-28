package com.chamrong.iecommerce.auth.infrastructure.security;

import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.auth.infrastructure.aop.MdcLoggingFilter;
import com.chamrong.iecommerce.auth.infrastructure.ratelimit.IpRateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration.
 *
 * <h3>Filter order</h3>
 *
 * <ol>
 *   <li>{@link MdcLoggingFilter} — MDC population (requestId, tenantId, clientIp) — runs first
 *   <li>{@link IpRateLimitFilter} — IP-based rate limiting (OWASP A07)
 *   <li>{@link BearerTokenAuthenticationFilter} — JWT extraction &amp; SecurityContext population
 *   <li>{@link TenantContextFilter} — Tenant ID propagation from JWT claim
 * </ol>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final TenantContextFilter tenantContextFilter;
  private final CustomAuthenticationEntryPoint authenticationEntryPoint;
  private final CustomAccessDeniedHandler accessDeniedHandler;
  private final IpRateLimitFilter ipRateLimitFilter;

  public SecurityConfig(
      TenantContextFilter tenantContextFilter,
      CustomAuthenticationEntryPoint authenticationEntryPoint,
      CustomAccessDeniedHandler accessDeniedHandler,
      IpRateLimitFilter ipRateLimitFilter) {
    this.tenantContextFilter = tenantContextFilter;
    this.authenticationEntryPoint = authenticationEntryPoint;
    this.accessDeniedHandler = accessDeniedHandler;
    this.ipRateLimitFilter = ipRateLimitFilter;
  }

  /**
   * Registers the MDC logging filter at the very highest servlet filter order so every subsequent
   * filter and handler benefits from the populated MDC context.
   */
  @Bean
  public FilterRegistrationBean<MdcLoggingFilter> mdcLoggingFilterRegistration() {
    final FilterRegistrationBean<MdcLoggingFilter> reg =
        new FilterRegistrationBean<>(new MdcLoggingFilter());
    reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
    reg.addUrlPatterns("/*");
    return reg;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.cors(org.springframework.security.config.Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .headers(
            headers ->
                headers
                    .contentSecurityPolicy(
                        csp ->
                            csp.policyDirectives(
                                "default-src 'self'; frame-ancestors 'none'; sandbox"))
                    .frameOptions(frameOptions -> frameOptions.deny())
                    .xssProtection(xss -> xss.disable()) // CSP handles XSS
                    .httpStrictTransportSecurity(
                        hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31_536_000))
                    .referrerPolicy(
                        ref ->
                            ref.policy(
                                org.springframework.security.web.header.writers
                                    .ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
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
        // ① MDC filter registered as servlet-level filter (FilterRegistrationBean above)
        // ② IP Rate Limiter — before any auth processing
        .addFilterBefore(ipRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
        // ③ TenantContextFilter — after Spring sets the SecurityContext
        .addFilterAfter(tenantContextFilter, BearerTokenAuthenticationFilter.class)
        .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(
            auth ->
                auth
                    // ── Public auth endpoints ────────────────────────────────────
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/**")
                    .permitAll()
                    // Social providers list — public (needed before login)
                    .requestMatchers(HttpMethod.GET, "/api/v1/auth/social/providers")
                    .permitAll()
                    // Self-service tenant registration
                    .requestMatchers(HttpMethod.POST, "/api/v1/tenants/register")
                    .permitAll()
                    // OpenAPI / Swagger
                    .requestMatchers(
                        "/v3/api-docs/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    // Custom error pages
                    .requestMatchers("/error", "/error/**")
                    .permitAll()
                    // ── Session management — authenticated users only ─────────────
                    .requestMatchers("/api/v1/users/me/sessions", "/api/v1/users/me/sessions/**")
                    .authenticated()
                    // ── 2FA, OTP, passkey — authenticated users only ─────────────
                    .requestMatchers("/api/v1/users/me/2fa", "/api/v1/users/me/passkey")
                    .authenticated()
                    // ── Permission-gated endpoints (defence-in-depth) ────────────
                    .requestMatchers(HttpMethod.GET, "/api/v1/users", "/api/v1/users/*")
                    .hasAuthority(Permissions.USER_READ)
                    .requestMatchers(HttpMethod.POST, "/api/v1/users")
                    .hasAuthority(Permissions.STAFF_MANAGE)
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
                    // ── Deny-by-Default ───────────────────────────────────────────
                    .anyRequest()
                    .authenticated())
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
