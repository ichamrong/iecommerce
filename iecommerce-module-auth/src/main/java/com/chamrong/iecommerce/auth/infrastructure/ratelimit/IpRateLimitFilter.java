package com.chamrong.iecommerce.auth.infrastructure.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * IP-based sliding-window rate limiter for sensitive authentication endpoints.
 *
 * <h3>Protected routes and limits (configurable)</h3>
 *
 * <ul>
 *   <li>POST {@code /api/v1/auth/login} — {@value #BUCKET_KEY_LOGIN} tokens/minute per IP
 *   <li>POST {@code /api/v1/auth/forgot-password} — tokens/hour per IP
 *   <li>POST {@code /api/v1/tenants/register} — tokens/day per IP
 * </ul>
 *
 * <p>Exceeding the limit returns HTTP {@code 429 Too Many Requests} with a {@code Retry-After}
 * header (OWASP A07 — Identification and Authentication Failures, A09 — Security Logging).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpRateLimitFilter extends OncePerRequestFilter {

  private static final String BUCKET_KEY_LOGIN = "login";
  private static final String BUCKET_KEY_FORGOT = "forgot";
  private static final String BUCKET_KEY_SIGNUP = "signup";

  private static final String PATH_LOGIN = "/api/v1/auth/login";
  private static final String PATH_FORGOT = "/api/v1/auth/forgot-password";
  private static final String PATH_SIGNUP = "/api/v1/tenants/register";

  private final RateLimitProperties props;

  // One cache per bucket category — key: "IP:category"
  private final Cache<String, Bucket> bucketCache =
      Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).maximumSize(10_000).build();

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    if (!"POST".equalsIgnoreCase(request.getMethod())) {
      filterChain.doFilter(request, response);
      return;
    }

    String path = request.getServletPath();
    String ip = extractClientIp(request);

    String bucketKey = null;
    long retryAfterSeconds = 0;

    if (path.equals(PATH_LOGIN)) {
      bucketKey = ip + ":" + BUCKET_KEY_LOGIN;
      retryAfterSeconds = 60;
    } else if (path.equals(PATH_FORGOT)) {
      bucketKey = ip + ":" + BUCKET_KEY_FORGOT;
      retryAfterSeconds = 3600;
    } else if (path.equals(PATH_SIGNUP)) {
      bucketKey = ip + ":" + BUCKET_KEY_SIGNUP;
      retryAfterSeconds = 86_400;
    }

    if (bucketKey == null) {
      filterChain.doFilter(request, response);
      return;
    }

    Bucket bucket = getBucket(bucketKey, path);
    if (bucket.tryConsume(1)) {
      filterChain.doFilter(request, response);
    } else {
      log.warn("Rate limit exceeded for IP '{}' on path '{}'.", ip, path);
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
      response
          .getWriter()
          .write(
              "{\"error\":\"too_many_requests\","
                  + "\"message\":\"Too many requests. Please try again later.\","
                  + "\"retry_after\":"
                  + retryAfterSeconds
                  + "}");
    }
  }

  private Bucket getBucket(String key, String path) {
    return bucketCache.get(key, k -> buildBucket(path));
  }

  private Bucket buildBucket(String path) {
    Bandwidth limit;
    if (path.equals(PATH_LOGIN)) {
      limit =
          Bandwidth.builder()
              .capacity(props.loginMaxPerMinute())
              .refillGreedy(props.loginMaxPerMinute(), Duration.ofMinutes(1))
              .build();
    } else if (path.equals(PATH_FORGOT)) {
      limit =
          Bandwidth.builder()
              .capacity(props.forgotPasswordMaxPerHour())
              .refillGreedy(props.forgotPasswordMaxPerHour(), Duration.ofHours(1))
              .build();
    } else {
      // signup
      limit =
          Bandwidth.builder()
              .capacity(props.signupMaxPerDay())
              .refillGreedy(props.signupMaxPerDay(), Duration.ofDays(1))
              .build();
    }
    return Bucket.builder().addLimit(limit).build();
  }

  /**
   * Extracts the real client IP, respecting common reverse-proxy headers.
   *
   * <p>Reads {@code X-Forwarded-For} first; falls back to {@code getRemoteAddr()}.
   */
  private String extractClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      // X-Forwarded-For: client, proxy1, proxy2 — take the first (client) IP
      return xff.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
