package com.chamrong.iecommerce.auth.infrastructure.security;

import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantRepository;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Extracts the tenantId from the validated Keycloak JWT and populates the local ThreadLocal
 * TenantContext. Block requests if the target tenant is suspended/disabled. This runs AFTER the
 * BearerTokenAuthenticationFilter.
 */
@Component
public class TenantContextFilter extends OncePerRequestFilter {

  private final TenantRepository tenantRepository;

  public TenantContextFilter(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication instanceof JwtAuthenticationToken jwtToken) {
      String tenantId = jwtToken.getToken().getClaimAsString("tenantId");
      if (tenantId != null && !tenantId.isBlank()) {

        if ("SYSTEM".equals(tenantId)) {
          TenantContext.setCurrentTenant(tenantId);
        } else {
          Optional<Tenant> tenantOpt = tenantRepository.findByCode(tenantId);

          if (tenantOpt.isEmpty() || !tenantOpt.get().isEnabled()) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN, "Tenant is suspended or disabled.");
            return; // Halt the filter chain
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
