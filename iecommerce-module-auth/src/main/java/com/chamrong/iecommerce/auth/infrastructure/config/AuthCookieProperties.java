package com.chamrong.iecommerce.auth.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for auth tokens stored in httpOnly cookies (ASVS-aligned).
 *
 * <p>When enabled, login and refresh responses set access_token and refresh_token in httpOnly,
 * Secure, SameSite cookies so the browser sends them automatically; tokens are not readable by
 * JavaScript (XSS cannot steal them).
 *
 * @see <a href="https://owasp.org/www-project-application-security-verification-standard/">OWASP
 *     ASVS</a>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.auth.cookies")
public class AuthCookieProperties {

  /** When true, login/refresh set httpOnly cookies in addition to returning JSON body. */
  private boolean enabled = true;

  /** Cookie name for the access token (JWT). */
  private String accessTokenName = "access_token";

  /** Cookie name for the refresh token. */
  private String refreshTokenName = "refresh_token";

  /** Max age in seconds for the access_token cookie. Should match JWT expiry (e.g. 300–3600). */
  private int accessTokenMaxAgeSeconds = 3600;

  /** Max age in seconds for the refresh_token cookie (e.g. 7 days). */
  private int refreshTokenMaxAgeSeconds = 604_800;

  /**
   * SameSite attribute: Strict, Lax, or None. Use Lax for cross-site top-level navigations (e.g.
   * link from email); use Strict for same-site only.
   */
  private String sameSite = "Lax";

  /** When true, cookies have Secure flag (HTTPS only). Set false for localhost. */
  private boolean secure = false;
}
