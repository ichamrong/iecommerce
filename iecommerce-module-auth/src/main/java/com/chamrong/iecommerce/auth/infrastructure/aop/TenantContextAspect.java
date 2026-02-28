package com.chamrong.iecommerce.auth.infrastructure.aop;

import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.annotation.WithTenantContext;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TenantContextAspect {

  private final ExpressionParser parser = new SpelExpressionParser();

  @Around("@annotation(withTenantContext)")
  public Object manageTenantContext(
      ProceedingJoinPoint joinPoint, WithTenantContext withTenantContext) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Object[] args = joinPoint.getArgs();
    String[] parameterNames = signature.getParameterNames();

    try {
      String tenantId = extractTenantId(withTenantContext.tenantId(), parameterNames, args);
      if (tenantId != null && !tenantId.isBlank()) {
        log.trace("Setting TenantContext to: {} for method: {}", tenantId, method.getName());
        TenantContext.setCurrentTenant(tenantId);
      } else {
        log.warn(
            "Could not extract tenantId for method: {} using expression: {}",
            method.getName(),
            withTenantContext.tenantId());
      }

      return joinPoint.proceed();
    } finally {
      log.trace("Clearing TenantContext after method: {}", method.getName());
      TenantContext.clear();
    }
  }

  private String extractTenantId(String expressionStr, String[] parameterNames, Object[] args) {
    if (expressionStr == null || expressionStr.isBlank()) {
      return null;
    }

    StandardEvaluationContext context = new StandardEvaluationContext();
    for (int i = 0; i < parameterNames.length; i++) {
      context.setVariable(parameterNames[i], args[i]);
      context.setVariable("p" + i, args[i]); // Support #p0 syntax
    }

    try {
      return parser.parseExpression(expressionStr).getValue(context, String.class);
    } catch (Exception e) {
      log.error("Failed to parse tenantId expression: {}", expressionStr, e);
      return null;
    }
  }
}
