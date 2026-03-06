package com.chamrong.iecommerce.auth.infrastructure.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

  @Mock private ObjectMapper objectMapper;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  @InjectMocks private CustomAccessDeniedHandler handler;

  @Test
  void handleShouldRedirectBrowserClientsTo403Page() throws IOException {
    when(request.getHeader("Accept")).thenReturn(MediaType.TEXT_HTML_VALUE);

    handler.handle(request, response, new AccessDeniedException("forbidden"));

    verify(response).sendRedirect("/error/403");
    verify(objectMapper, never()).writeValue(any(OutputStream.class), any());
  }

  @Test
  void handleShouldReturnJsonForApiClients() throws IOException {
    when(request.getHeader("Accept")).thenReturn(MediaType.APPLICATION_JSON_VALUE);
    when(request.getRequestURI()).thenReturn("/api/auth");

    handler.handle(request, response, new AccessDeniedException("forbidden"));

    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    verify(objectMapper).writeValue(nullable(OutputStream.class), any(Map.class));
  }
}
