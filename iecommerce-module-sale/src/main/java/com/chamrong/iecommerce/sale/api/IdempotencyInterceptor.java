package com.chamrong.iecommerce.sale.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Placeholder interceptor for idempotency header handling.
 *
 * <p>Currently, idempotency is enforced at the application/use-case layer via {@code
 * IdempotentExecutor}. This interceptor simply exposes the header for future use.
 */
@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    // Header is intentionally not acted on here; use cases handle idempotency.
    return true;
  }
}
