package com.chamrong.iecommerce.auth.infrastructure.security;

import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantRepository;
import com.chamrong.iecommerce.auth.domain.TenantStatus;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Stable error codes for tenant status enforcement (ASVS). */
final class TenantAccessErrorCodes {
  static final String TENANT_SUSPENDED = "TENANT_SUSPENDED";
  static final String TENANT_TERMINATED = "TENANT_TERMINATED";
  static final String TENANT_GRACE_READ_ONLY = "TENANT_GRACE_READ_ONLY";
}

/**
 * Extracts the tenantId from the validated Keycloak JWT and populates the local ThreadLocal
 * TenantContext. Blocks requests when tenant is SUSPENDED/TERMINATED; enforces read-only for GRACE.
 * Runs AFTER BearerTokenAuthenticationFilter.
 */
@Component
public class TenantContextFilter extends OncePerRequestFilter {

  private final TenantRepository tenantRepository;

  public TenantContextFilter(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication instanceof JwtAuthenticationToken jwtToken) {
      String tenantId = jwtToken.getToken().getClaimAsString("tenantId");
      if (tenantId != null && !tenantId.isBlank()) {

        if ("SYSTEM".equals(tenantId)) {
          TenantContext.setCurrentTenant(tenantId);
        } else {
          Optional<Tenant> tenantOpt = tenantRepository.findByCode(tenantId);

          if (tenantOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant not found.");
            response.setHeader("X-Error-Code", TenantAccessErrorCodes.TENANT_SUSPENDED);
            return;
          }
          Tenant tenant = tenantOpt.get();
          TenantStatus status = tenant.getStatus();

          // SUSPENDED, TERMINATED, DISABLED: no API access
          if (status == TenantStatus.SUSPENDED
              || status == TenantStatus.TERMINATED
              || status == TenantStatus.DISABLED) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                status == TenantStatus.TERMINATED
                    ? "Tenant is terminated."
                    : "Tenant is suspended.");
            response.setHeader(
                "X-Error-Code",
                status == TenantStatus.TERMINATED
                    ? TenantAccessErrorCodes.TENANT_TERMINATED
                    : TenantAccessErrorCodes.TENANT_SUSPENDED);
            return;
          }

          // GRACE: allow only read-only (GET, HEAD, OPTIONS)
          if (status == TenantStatus.GRACE) {
            String method = request.getMethod();
            if (!("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method))) {
              response.sendError(
                  HttpServletResponse.SC_FORBIDDEN,
                  "Tenant in grace period; write operations are not allowed.");
              response.setHeader("X-Error-Code", TenantAccessErrorCodes.TENANT_GRACE_READ_ONLY);
              return;
            }
          }

          if (!tenant.isEnabled()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant is disabled.");
            response.setHeader("X-Error-Code", TenantAccessErrorCodes.TENANT_SUSPENDED);
            return;
          }
          TenantContext.setCurrentTenant(tenantId);
        }
      }
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
  }
}
