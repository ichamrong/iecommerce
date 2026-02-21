package com.chamrong.iecommerce.auth.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Custom handler that serves a Thymeleaf 403 page for browser requests
 * and a JSON response for API/programmatic clients.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {

    String acceptHeader = request.getHeader("Accept");
    boolean isBrowserRequest =
        acceptHeader != null && acceptHeader.contains(MediaType.TEXT_HTML_VALUE);

    if (isBrowserRequest) {
      response.sendRedirect("/error/403");
    } else {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      objectMapper.writeValue(
          response.getOutputStream(),
          Map.of(
              "status", 403,
              "error", "Forbidden",
              "message", "You do not have permission to access this resource.",
              "timestamp", Instant.now().toString(),
              "path", request.getRequestURI()));
    }
  }
}
