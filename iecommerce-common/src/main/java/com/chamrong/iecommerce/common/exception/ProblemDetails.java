package com.chamrong.iecommerce.common.exception;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

/** RFC 7807 Problem Details for safe and standard error reporting (BG9). */
@Value
@Builder
public class ProblemDetails {
  URI type;
  String title;
  int status;
  String detail;
  URI instance;
  String errorCode;
  Instant timestamp;
  Map<String, Object> extensions;
}
