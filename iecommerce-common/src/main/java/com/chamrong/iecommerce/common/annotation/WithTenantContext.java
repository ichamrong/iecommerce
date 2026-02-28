package com.chamrong.iecommerce.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that require Multi-Tenant context management via AOP. The aspect will
 * look for a tenant ID in the method arguments (e.g., in a command object).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithTenantContext {
  /**
   * SpEL expression to extract the tenant ID from method arguments. Default looks for a field or
   * method named 'tenantId' in the first argument.
   */
  String tenantId() default "#p0.tenantId";
}
