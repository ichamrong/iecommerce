package com.chamrong.iecommerce.auth.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that audits all command and query handler invocations.
 *
 * <h3>What it records</h3>
 *
 * <ul>
 *   <li>Handler class + method name
 *   <li>Execution duration in milliseconds
 *   <li>Success or failure (exception type + message on failure)
 * </ul>
 *
 * <h3>Pointcut</h3>
 *
 * <p>Matches any {@code handle()} method inside the {@code application.command} or {@code
 * application.query} packages, including sub-packages.
 *
 * <p>MDC context ({@code requestId}, {@code tenantId}, {@code clientIp}) is already populated by
 * {@link MdcLoggingFilter} at the time this aspect executes.
 */
@Aspect
@Component
@Slf4j
public class HandlerAuditAspect {

  /** Pointcut: any method named {@code handle} in command or query packages. */
  @Around(
      "execution(* com.chamrong.iecommerce.auth.application.command..handle(..)) || "
          + "execution(* com.chamrong.iecommerce.auth.application.query..handle(..))")
  public Object auditHandler(final ProceedingJoinPoint pjp) throws Throwable {
    final MethodSignature sig = (MethodSignature) pjp.getSignature();
    final String handlerName =
        pjp.getTarget().getClass().getSimpleName() + "." + sig.getMethod().getName();

    final long start = System.currentTimeMillis();
    try {
      final Object result = pjp.proceed();
      final long elapsed = System.currentTimeMillis() - start;
      log.info("[HANDLER] {} completed in {}ms", handlerName, elapsed);
      return result;
    } catch (Exception ex) {
      final long elapsed = System.currentTimeMillis() - start;
      log.warn(
          "[HANDLER] {} FAILED after {}ms — {}: {}",
          handlerName,
          elapsed,
          ex.getClass().getSimpleName(),
          ex.getMessage());
      throw ex;
    }
  }
}
