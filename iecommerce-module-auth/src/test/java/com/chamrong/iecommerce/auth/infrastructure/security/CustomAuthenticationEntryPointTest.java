package com.chamrong.iecommerce.auth.infrastructure.security;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

  @Mock private ObjectMapper objectMapper;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  @InjectMocks private CustomAuthenticationEntryPoint entryPoint;

  @Test
  void commenceShouldRedirectBrowserClientsTo401Page() throws IOException {
    when(request.getHeader("Accept")).thenReturn(MediaType.TEXT_HTML_VALUE);

    entryPoint.commence(request, response, new BadCredentialsException("bad creds"));

    verify(response).sendRedirect("/error/401");
  }

  @Test
  void commenceShouldReturnJsonForApiClients() throws IOException {
    when(request.getHeader("Accept")).thenReturn(MediaType.APPLICATION_JSON_VALUE);
    when(request.getRequestURI()).thenReturn("/api/auth");

    entryPoint.commence(request, response, new BadCredentialsException("bad creds"));

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
  }
}
