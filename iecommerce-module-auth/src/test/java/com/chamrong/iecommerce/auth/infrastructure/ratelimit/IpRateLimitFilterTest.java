package com.chamrong.iecommerce.auth.infrastructure.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

/** Unit tests for {@link IpRateLimitFilter}. */
@ExtendWith(MockitoExtension.class)
class IpRateLimitFilterTest {

  private IpRateLimitFilter filter;

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  private StringWriter responseBody;

  @BeforeEach
  void setUp() throws IOException {
    filter = new IpRateLimitFilter();
    responseBody = new StringWriter();
  }

  @Test
  void nonPostRequestsShouldBypassRateLimiting() throws ServletException, IOException {
    when(request.getMethod()).thenReturn("GET");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void nonProtectedPostPathsShouldBypassRateLimiting() throws ServletException, IOException {
    when(request.getMethod()).thenReturn("POST");
    when(request.getServletPath()).thenReturn("/api/v1/other");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void loginPathShouldAllowWithinLimitAndBlockWhenExceeded() throws ServletException, IOException {
    when(request.getMethod()).thenReturn("POST");
    when(request.getServletPath()).thenReturn("/api/v1/auth/login");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(response.getWriter()).thenReturn(new PrintWriter(responseBody));

    // Within limit: first few calls pass through to the chain
    for (int i = 0; i < 5; i++) {
      filter.doFilterInternal(request, response, filterChain);
    }

    org.mockito.Mockito.verify(filterChain, org.mockito.Mockito.atLeastOnce())
        .doFilter(request, response);

    // Force bucket exhaustion by many more attempts; exact threshold depends on RateLimitProperties
    for (int i = 0; i < 200; i++) {
      filter.doFilterInternal(request, response, filterChain);
    }

    org.mockito.Mockito.verify(response, org.mockito.Mockito.atLeastOnce())
        .setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    org.mockito.Mockito.verify(response, org.mockito.Mockito.atLeastOnce())
        .setHeader("Retry-After", "60");
    assertThat(responseBody.toString()).contains("too_many_requests");
  }
}
