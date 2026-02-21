package com.chamrong.iecommerce.auth.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Custom entry point that serves a Thymeleaf 401 page for browser requests
 * and a JSON response for API/programmatic clients.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    String acceptHeader = request.getHeader("Accept");
    boolean isBrowserRequest =
        acceptHeader != null && acceptHeader.contains(MediaType.TEXT_HTML_VALUE);

    if (isBrowserRequest) {
      // Redirect browser to our custom 401 Thymeleaf page
      response.sendRedirect("/error/401");
    } else {
      // Return structured JSON for API clients (Swagger, curl, mobile apps)
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      objectMapper.writeValue(
          response.getOutputStream(),
          Map.of(
              "status", 401,
              "error", "Unauthorized",
              "message", "Authentication required. Please provide a valid Bearer token.",
              "timestamp", Instant.now().toString(),
              "path", request.getRequestURI()));
    }
  }
}
