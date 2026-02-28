package com.chamrong.iecommerce.common.infrastructure.aop;

import com.chamrong.iecommerce.common.annotation.Masked;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Aspect
@Component
@Slf4j
public class MaskingLoggingAspect {

  @Before(
      "within(com.chamrong.iecommerce..*) && (execution(* *.handle(..)) || execution(*"
          + " *.register*(..)) || execution(* *.login*(..)))")
  public void logMethodEntry(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = signature.getName();
    Object[] args = joinPoint.getArgs();
    Parameter[] parameters = signature.getMethod().getParameters();

    StringBuilder sb = new StringBuilder();
    sb.append("Entering ").append(className).append(".").append(methodName).append("(");

    for (int i = 0; i < args.length; i++) {
      if (i > 0) sb.append(", ");

      String paramName = parameters[i].getName();
      Object value = args[i];

      sb.append(paramName).append("=");
      if (value == null) {
        sb.append("null");
      } else if (parameters[i].isAnnotationPresent(Masked.class)) {
        sb.append("****");
      } else if (isSensitiveParam(paramName)) {
        sb.append("****");
      } else {
        sb.append(maskValue(value));
      }
    }
    sb.append(")");
    log.info(sb.toString());
  }

  private boolean isSensitiveParam(String name) {
    String lower = name.toLowerCase();
    return lower.contains("password")
        || lower.contains("secret")
        || lower.contains("token")
        || lower.contains("credential");
  }

  private Object maskValue(Object value) {
    if (value == null) return null;

    if (value instanceof String s) {
      return s;
    }

    if (value instanceof Number || value instanceof Boolean) {
      return value;
    }

    if (value instanceof Collection<?> || value instanceof Map<?, ?>) {
      return "[HIDDEN_COLLECTION]";
    }

    // For complex objects, use reflection to find @Masked fields
    return processObject(value);
  }

  private String processObject(Object value) {
    String original = value.toString();
    // Start with basic regex masking for known patterns in toString()
    String masked =
        original
            .replaceAll("password=[^,\\)]+", "password=****")
            .replaceAll("email=[^,\\)]+", "email=****");

    // Then use reflection to specifically look for @Masked fields if it's a POJO/Record
    try {
      StringBuilder builder = new StringBuilder(value.getClass().getSimpleName()).append("[");
      Field[] fields = value.getClass().getDeclaredFields();
      boolean first = true;
      for (Field field : fields) {
        if (!first) builder.append(", ");
        first = false;

        ReflectionUtils.makeAccessible(field);
        Object fieldValue = field.get(value);
        builder.append(field.getName()).append("=");

        if (field.isAnnotationPresent(Masked.class)) {
          builder.append("****");
        } else {
          builder.append(fieldValue);
        }
      }
      builder.append("]");
      return builder.toString();
    } catch (Exception e) {
      // Fallback to regex-masked toString
      return masked;
    }
  }
}
