package com.chamrong.iecommerce.auth.infrastructure.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for IP-based rate limiting.
 *
 * <p>Defaults are conservative values suitable for production. Override in {@code application.yml}:
 *
 * <pre>{@code
 * iecommerce:
 *   security:
 *     rate-limit:
 *       login-max-per-minute: 10
 *       forgot-password-max-per-hour: 5
 *       signup-max-per-day: 3
 * }</pre>
 */
@ConfigurationProperties("iecommerce.security.rate-limit")
public record RateLimitProperties(
    int loginMaxPerMinute, int forgotPasswordMaxPerHour, int signupMaxPerDay) {

  /** Provides safe defaults when the property block is absent from configuration. */
  public RateLimitProperties() {
    this(10, 5, 3);
  }
}
