package com.chamrong.iecommerce.common.logging;

import org.slf4j.MDC;

/**
 * Central place for structured logging: correlation ID, tenant ID, request ID. Do not log PII; use
 * masked or hashed values for user identifiers in log context.
 */
public final class LoggingHelper {

  public static final String MDC_CORRELATION_ID = "correlationId";
  public static final String MDC_TENANT_ID = "tenantId";
  public static final String MDC_REQUEST_ID = "requestId";

  private LoggingHelper() {}

  /**
   * Puts correlationId, tenantId, and requestId into MDC for the current thread. Call at request
   * entry (e.g. filter); clear in finally.
   *
   * @param correlationId optional; can be from header or generated
   * @param tenantId optional; from TenantContext
   * @param requestId optional; can equal correlationId or be separate
   */
  public static void putContext(String correlationId, String tenantId, String requestId) {
    if (correlationId != null && !correlationId.isBlank()) {
      MDC.put(MDC_CORRELATION_ID, correlationId);
    }
    if (tenantId != null && !tenantId.isBlank()) {
      MDC.put(MDC_TENANT_ID, tenantId);
    }
    if (requestId != null && !requestId.isBlank()) {
      MDC.put(MDC_REQUEST_ID, requestId);
    }
  }

  /** Removes correlationId, tenantId, requestId from MDC. Call in finally. */
  public static void clearContext() {
    MDC.remove(MDC_CORRELATION_ID);
    MDC.remove(MDC_TENANT_ID);
    MDC.remove(MDC_REQUEST_ID);
  }

  /** Returns correlation ID from MDC (e.g. for audit). Null if not set. */
  public static String getCorrelationId() {
    return MDC.get(MDC_CORRELATION_ID);
  }

  /** Returns tenant ID from MDC. Null if not set. Prefer TenantContext.getCurrentTenant() when available. */
  public static String getTenantIdFromMdc() {
    return MDC.get(MDC_TENANT_ID);
  }
}
