package com.chamrong.iecommerce.common.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lightweight Token Bucket rate limiter to protect sensitive endpoints. In a production bank
 * environment, this would use Redis for distributed limiting.
 */
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

  private static final long CAPACITY = 10; // 10 requests
  private static final long REFILL_RATE_MS = 1000; // 1 request per second

  private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    if (isSensitiveEndpoint(path)) {
      String clientIp = request.getRemoteAddr();
      TokenBucket bucket = buckets.computeIfAbsent(clientIp, k -> new TokenBucket());

      if (!bucket.tryConsume()) {
        log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("Too many requests. Please try again later.");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private boolean isSensitiveEndpoint(String path) {
    return path.contains("/login") || path.contains("/signup") || path.contains("/password");
  }

  private static class TokenBucket {
    private final AtomicLong tokens = new AtomicLong(CAPACITY);
    private long lastRefillTimestamp = System.currentTimeMillis();

    synchronized boolean tryConsume() {
      refill();
      if (tokens.get() > 0) {
        tokens.decrementAndGet();
        return true;
      }
      return false;
    }

    private void refill() {
      long now = System.currentTimeMillis();
      long delta = now - lastRefillTimestamp;
      long refillTokens = delta / REFILL_RATE_MS;

      if (refillTokens > 0) {
        tokens.set(Math.min(CAPACITY, tokens.get() + refillTokens));
        lastRefillTimestamp = now;
      }
    }
  }
}
