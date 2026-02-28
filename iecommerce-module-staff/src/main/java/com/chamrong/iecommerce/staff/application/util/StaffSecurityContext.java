package com.chamrong.iecommerce.staff.application.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utility that extracts the current authenticated user's ID from the Spring Security context.
 *
 * <p>The JWT {@code sub} claim is used as the canonical actor identifier, matching Keycloak's user
 * UUID.
 *
 * <p>Falls back to {@code "system"} only in non-authenticated contexts (e.g., background jobs,
 * startup Liquibase migrations). A {@code RuntimeException} is NOT thrown on missing authentication
 * intentionally — audit writes should never block the main operation.
 */
public final class StaffSecurityContext {

  static final String FALLBACK_ACTOR = "system";

  private StaffSecurityContext() {}

  /**
   * Returns the {@code sub} claim of the currently authenticated JWT principal.
   *
   * @return user ID string, or {@code "system"} if no authenticated context is available
   */
  public static String currentActorId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      String sub = jwt.getSubject();
      return (sub != null && !sub.isBlank()) ? sub : FALLBACK_ACTOR;
    }
    return FALLBACK_ACTOR;
  }
}
