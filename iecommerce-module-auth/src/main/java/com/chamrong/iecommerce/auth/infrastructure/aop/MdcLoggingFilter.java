package com.chamrong.iecommerce.auth.infrastructure.aop;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that populates the SLF4J MDC at the start of every HTTP request.
 *
 * <h3>MDC keys populated</h3>
 *
 * <table>
 *   <tr><th>Key</th><th>Value</th></tr>
 *   <tr><td>{@code requestId}</td><td>{@code X-Request-ID} header, or new UUID if absent</td></tr>
 *   <tr><td>{@code tenantId}</td><td>{@code X-Tenant-ID} header (if present)</td></tr>
 *   <tr><td>{@code clientIp}</td><td>Real client IP (X-Forwarded-For → REMOTE_ADDR)</td></tr>
 * </table>
 *
 * <p>The MDC is cleared in the {@code finally} block to prevent context leakage between requests on
 * pooled threads (e.g. Tomcat thread pool).
 *
 * <p>The {@code X-Request-ID} is also written back to the response header so that clients and API
 * gateways can correlate requests to log entries.
 *
 * <p>This filter runs at {@link org.springframework.core.Ordered#HIGHEST_PRECEDENCE + 1} — after
 * Spring's own bootstrapping but before everything else — ensuring MDC fields are available to all
 * downstream filters and handlers.
 */
@Slf4j
public class MdcLoggingFilter extends OncePerRequestFilter {

  private static final String REQUEST_ID_HEADER = "X-Request-ID";
  private static final String TENANT_ID_HEADER = "X-Tenant-ID";

  @Override
  protected void doFilterInternal(
      @NonNull final HttpServletRequest request,
      @NonNull final HttpServletResponse response,
      @NonNull final FilterChain chain)
      throws ServletException, IOException {

    final String requestId = resolveRequestId(request);
    final String tenantId = request.getHeader(TENANT_ID_HEADER);
    final String clientIp = resolveClientIp(request);

    try {
      MDC.put("requestId", requestId);
      MDC.put("clientIp", clientIp);
      if (tenantId != null && !tenantId.isBlank()) {
        MDC.put("tenantId", tenantId);
      }

      // Echo the request ID back to the caller for log correlation
      response.setHeader(REQUEST_ID_HEADER, requestId);

      chain.doFilter(request, response);

    } finally {
      MDC.clear();
    }
  }

  /**
   * Extracts or generates the request correlation ID. If the client sends {@code X-Request-ID}, we
   * use it (after length-capping for safety). Otherwise, we generate a new UUID.
   */
  private static String resolveRequestId(final HttpServletRequest request) {
    final String header = request.getHeader(REQUEST_ID_HEADER);
    if (header != null && !header.isBlank()) {
      // Cap at 64 chars — prevent log injection via oversized headers
      return header.length() <= 64 ? header : header.substring(0, 64);
    }
    return UUID.randomUUID().toString();
  }

  /**
   * Resolves the real client IP, accounting for reverse proxies. Reads {@code X-Forwarded-For}
   * (first address in list = original client).
   */
  private static String resolveClientIp(final HttpServletRequest request) {
    final String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      // X-Forwarded-For: client, proxy1, proxy2 — take only the first
      final int comma = xff.indexOf(',');
      return (comma > 0 ? xff.substring(0, comma) : xff).trim();
    }
    return request.getRemoteAddr();
  }
}
