package com.chamrong.iecommerce.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark fields that contains sensitive data (PII) and should be masked in logs.
 * Example: emails, passwords, phone numbers.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Masked {
  /** Pattern to use for masking. DEFAULT: p****d */
  String pattern() default "CHAR_MASK";
}
