package com.chamrong.iecommerce.auth.infrastructure.config;

import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * Helper to set and clear auth tokens in httpOnly cookies (ASVS-aligned storage).
 *
 * <p>Call after login/refresh to set cookies; call on logout to clear them.
 */
@Component
public class AuthCookieHelper {

  private final AuthCookieProperties properties;

  public AuthCookieHelper(AuthCookieProperties properties) {
    this.properties = properties;
  }

  /**
   * Adds access_token and refresh_token cookies to the response.
   *
   * @param response the HTTP response
   * @param auth the token response from login/refresh
   */
  public void addAuthCookies(HttpServletResponse response, AuthResponse auth) {
    if (!properties.isEnabled()) {
      return;
    }
    addCookie(
        response,
        properties.getAccessTokenName(),
        auth.accessToken(),
        properties.getAccessTokenMaxAgeSeconds());
    addCookie(
        response,
        properties.getRefreshTokenName(),
        auth.refreshToken(),
        properties.getRefreshTokenMaxAgeSeconds());
  }

  /** Clears the auth cookies (e.g. on logout). */
  public void clearAuthCookies(HttpServletResponse response) {
    if (!properties.isEnabled()) {
      return;
    }
    clearCookie(response, properties.getAccessTokenName());
    clearCookie(response, properties.getRefreshTokenName());
  }

  private void addCookie(
      HttpServletResponse response, String name, String value, int maxAgeSeconds) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(properties.isSecure());
    cookie.setPath("/");
    cookie.setMaxAge(maxAgeSeconds);
    if ("None".equalsIgnoreCase(properties.getSameSite())) {
      response.addHeader("Set-Cookie", buildSetCookieHeader(cookie, "None"));
      return;
    }
    if ("Strict".equalsIgnoreCase(properties.getSameSite())) {
      response.addHeader("Set-Cookie", buildSetCookieHeader(cookie, "Strict"));
      return;
    }
    response.addHeader("Set-Cookie", buildSetCookieHeader(cookie, "Lax"));
  }

  private String buildSetCookieHeader(Cookie cookie, String sameSite) {
    return cookie.getName()
        + "="
        + cookie.getValue()
        + "; Path="
        + cookie.getPath()
        + "; Max-Age="
        + cookie.getMaxAge()
        + "; HttpOnly"
        + (cookie.getSecure() ? "; Secure" : "")
        + "; SameSite="
        + sameSite;
  }

  private void clearCookie(HttpServletResponse response, String name) {
    Cookie cookie = new Cookie(name, "");
    cookie.setHttpOnly(true);
    cookie.setSecure(properties.isSecure());
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addHeader("Set-Cookie", buildSetCookieHeader(cookie, properties.getSameSite()));
  }

  /**
   * Reads the refresh token from the request cookies (used when client sends credentials and does
   * not put refresh token in body).
   */
  public String getRefreshTokenFromCookies(jakarta.servlet.http.HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }
    return Arrays.stream(request.getCookies())
        .filter(c -> properties.getRefreshTokenName().equals(c.getName()))
        .findFirst()
        .map(Cookie::getValue)
        .orElse(null);
  }

  public String getAccessTokenCookieName() {
    return properties.getAccessTokenName();
  }

  public boolean isEnabled() {
    return properties.isEnabled();
  }
}
