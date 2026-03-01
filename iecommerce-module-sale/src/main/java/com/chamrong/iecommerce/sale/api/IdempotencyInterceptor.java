package com.chamrong.iecommerce.sale.api;

import com.chamrong.iecommerce.sale.domain.service.IdempotencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

  private final IdempotencyService idempotencyService;

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    String key = request.getHeader("Idempotency-Key");
    if (key == null) return true;

    // In a real implementation, we would check if this key has a stored response
    // and return it immediately if found.
    // For now, we'll just allow it and the UseCase will handle the logic.
    return true;
  }
}
