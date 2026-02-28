package com.chamrong.iecommerce.asset.infrastructure.aop;

import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.StorageException;
import com.chamrong.iecommerce.common.EventDispatcher;
import com.chamrong.iecommerce.common.event.StorageOperationEvent;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class StorageAuditAspect {

  private final EventDispatcher eventDispatcher;

  @Around(
      "execution(* com.chamrong.iecommerce.asset.domain.StorageService.*(..)) && !execution(*"
          + " com.chamrong.iecommerce.asset.domain.StorageService.getProviderName(..))")
  public Object auditStorageOperation(ProceedingJoinPoint joinPoint) throws Throwable {
    StorageService storageService = (StorageService) joinPoint.getTarget();
    String provider = storageService.getProviderName();
    String operation = joinPoint.getSignature().getName();

    // Traceability metadata
    String correlationId = getCorrelationId();
    String clientIp = getClientIp();

    log.debug(
        "[{}] Starting storage operation: {} on provider: {} from IP: {}",
        correlationId,
        operation,
        provider,
        clientIp);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    try {
      Object result = joinPoint.proceed();
      stopWatch.stop();
      long duration = stopWatch.getTotalTimeMillis();

      log.info(
          "[{}] Completed storage operation: {} on provider: {} in {}ms",
          correlationId,
          operation,
          provider,
          duration);

      dispatchSuccess(
          provider,
          operation,
          duration,
          correlationId,
          clientIp,
          storageService.getClass().getSimpleName());

      return result;
    } catch (StorageException e) {
      stopWatch.stop();
      dispatchFailure(
          provider,
          operation,
          stopWatch.getTotalTimeMillis(),
          correlationId,
          clientIp,
          e.getMessage(),
          e.getErrorCode().getCode());
      throw e;
    } catch (Exception e) {
      stopWatch.stop();
      dispatchFailure(
          provider,
          operation,
          stopWatch.getTotalTimeMillis(),
          correlationId,
          clientIp,
          e.getMessage(),
          "UNKNOWN_ERROR");
      log.error(
          "[{}] Storage operation failed: {} on provider: {}",
          correlationId,
          operation,
          provider,
          e);
      throw new StorageException(AssetErrorCode.STORAGE_OPERATION_FAILED, e.getMessage());
    }
  }

  private void dispatchSuccess(
      String provider,
      String operation,
      long duration,
      String correlationId,
      String clientIp,
      String delegate) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("delegate", delegate);
    metadata.put("correlationId", correlationId);
    metadata.put("clientIp", clientIp);

    eventDispatcher.dispatch(
        new StorageOperationEvent(
            provider, operation, null, duration, "SUCCESS", null, metadata, Instant.now()));
  }

  private void dispatchFailure(
      String provider,
      String operation,
      long duration,
      String correlationId,
      String clientIp,
      String message,
      String errorCode) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("errorCode", errorCode);
    metadata.put("correlationId", correlationId);
    metadata.put("clientIp", clientIp);

    eventDispatcher.dispatch(
        new StorageOperationEvent(
            provider, operation, null, duration, "FAILURE", message, metadata, Instant.now()));
  }

  private String getCorrelationId() {
    return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
        .filter(ServletRequestAttributes.class::isInstance)
        .map(ServletRequestAttributes.class::cast)
        .map(ServletRequestAttributes::getRequest)
        .map(req -> req.getHeader("X-Correlation-ID"))
        .orElseGet(() -> UUID.randomUUID().toString());
  }

  private String getClientIp() {
    return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
        .filter(ServletRequestAttributes.class::isInstance)
        .map(ServletRequestAttributes.class::cast)
        .map(ServletRequestAttributes::getRequest)
        .map(HttpServletRequest::getRemoteAddr)
        .orElse("unknown");
  }
}
