package com.chamrong.iecommerce.auth.infrastructure.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantPlan;
import com.chamrong.iecommerce.auth.domain.TenantProvisioningStatus;
import com.chamrong.iecommerce.auth.domain.TenantStatus;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Lifecycle tests for TenantContextFilter: GRACE read-only, SUSPENDED/TERMINATED 403 with
 * X-Error-Code (per AUDIT_REMEDIATION_PLAN Tests to Add).
 */
@ExtendWith(MockitoExtension.class)
class TenantContextFilterTest {

  private static final String TENANT_ID = "tenant-T1";

  @Mock private TenantRepositoryPort tenantRepository;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  private TenantContextFilter filter;

  @BeforeEach
  void setUp() {
    filter = new TenantContextFilter(tenantRepository);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private static JwtAuthenticationToken jwtWithTenant(String tenantId) {
    Jwt jwt =
        Jwt.withTokenValue("mock-token")
            .header("alg", "RS256")
            .claim("tenantId", tenantId)
            .claim("sub", "user-1")
            .build();
    return new JwtAuthenticationToken(jwt, null);
  }

  private static Tenant tenantWithStatus(TenantStatus status) {
    Tenant t =
        Tenant.provision(
            TENANT_ID, "Test Tenant", TenantPlan.FREE, status, TenantProvisioningStatus.COMPLETED);
    return t;
  }

  @Test
  void whenNoTenantIdClaim_filterDoesNotCallRepository() throws Exception {
    Jwt jwt =
        Jwt.withTokenValue("mock-token").header("alg", "RS256").claim("sub", "user-1").build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, null));

    filter.doFilterInternal(request, response, filterChain);

    verify(tenantRepository, never()).findByCode(any());
    verify(filterChain).doFilter(any(), any());
  }

  @Test
  void whenTenantNotFound_returns403WithSuspendedCode() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(jwtWithTenant(TENANT_ID));
    when(tenantRepository.findByCode(TENANT_ID)).thenReturn(Optional.empty());

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), any());
    verify(response).setHeader("X-Error-Code", "TENANT_SUSPENDED");
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void whenTenantGrace_andGet_allowsRequest() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(jwtWithTenant(TENANT_ID));
    when(tenantRepository.findByCode(TENANT_ID))
        .thenReturn(Optional.of(tenantWithStatus(TenantStatus.GRACE)));
    when(request.getMethod()).thenReturn("GET");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(any(), any());
    verify(response, never()).sendError(anyInt(), any());
  }

  @Test
  void whenTenantGrace_andPost_returns403AndGraceReadOnlyCode() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(jwtWithTenant(TENANT_ID));
    when(tenantRepository.findByCode(TENANT_ID))
        .thenReturn(Optional.of(tenantWithStatus(TenantStatus.GRACE)));
    when(request.getMethod()).thenReturn("POST");

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), any());
    verify(response).setHeader("X-Error-Code", "TENANT_GRACE_READ_ONLY");
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void whenTenantSuspended_returns403AndSuspendedCode() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(jwtWithTenant(TENANT_ID));
    when(tenantRepository.findByCode(TENANT_ID))
        .thenReturn(Optional.of(tenantWithStatus(TenantStatus.SUSPENDED)));

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), any());
    verify(response).setHeader("X-Error-Code", "TENANT_SUSPENDED");
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void whenTenantTerminated_returns403AndTerminatedCode() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(jwtWithTenant(TENANT_ID));
    when(tenantRepository.findByCode(TENANT_ID))
        .thenReturn(Optional.of(tenantWithStatus(TenantStatus.TERMINATED)));

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), any());
    verify(response).setHeader("X-Error-Code", "TENANT_TERMINATED");
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void whenTenantDisabled_returns403AndSuspendedCode() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(jwtWithTenant(TENANT_ID));
    Tenant tenant = tenantWithStatus(TenantStatus.ACTIVE);
    tenant.setEnabled(false);
    when(tenantRepository.findByCode(TENANT_ID)).thenReturn(Optional.of(tenant));

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), any());
    verify(response).setHeader("X-Error-Code", "TENANT_SUSPENDED");
    verify(filterChain, never()).doFilter(any(), any());
  }
}
