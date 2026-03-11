package com.chamrong.iecommerce.auth.infrastructure.security;

import com.chamrong.iecommerce.auth.infrastructure.config.AuthCookieProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

/**
 * Resolves the JWT from either the {@code access_token} cookie (ASVS-aligned) or the {@code
 * Authorization: Bearer} header.
 *
 * <p>Cookie is checked first so browser-based clients can rely on httpOnly cookies without sending
 * the token in JS.
 *
 * <p>For public auth paths (login, register, forgot-password, refresh-token) we do not resolve a
 * token so that an expired/invalid cookie does not trigger 401 before the request is allowed by
 * permitAll().
 */
public class CookieBearerTokenResolver implements BearerTokenResolver {

  /**
   * Paths that are permitAll(); resolving a token here would cause expired cookie to trigger 401.
   */
  private static final Set<String> PUBLIC_AUTH_PATHS =
      Set.of(
          "/api/v1/auth/login",
          "/api/v1/auth/register",
          "/api/v1/auth/forgot-password",
          "/api/v1/auth/refresh-token");

  private final AuthCookieProperties cookieProperties;

  public CookieBearerTokenResolver(AuthCookieProperties cookieProperties) {
    this.cookieProperties = cookieProperties;
  }

  @Override
  public String resolve(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path != null && PUBLIC_AUTH_PATHS.contains(path)) {
      return null;
    }
    if (cookieProperties.isEnabled() && request.getCookies() != null) {
      String fromCookie =
          Arrays.stream(request.getCookies())
              .filter(c -> cookieProperties.getAccessTokenName().equals(c.getName()))
              .findFirst()
              .map(jakarta.servlet.http.Cookie::getValue)
              .orElse(null);
      if (fromCookie != null && !fromCookie.isBlank()) {
        return fromCookie;
      }
    }
    String auth = request.getHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      return auth.substring(7);
    }
    return null;
  }
}
