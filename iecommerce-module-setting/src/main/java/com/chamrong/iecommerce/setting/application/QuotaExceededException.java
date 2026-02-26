package com.chamrong.iecommerce.setting.application;

/**
 * Thrown when a tenant's usage has reached or exceeded the configured quota limit for a feature.
 *
 * <p>This is a domain-level exception — the global exception handler maps it to {@code 402 Payment
 * Required} or {@code 403 Forbidden} depending on context.
 */
public class QuotaExceededException extends RuntimeException {

  private final String quotaKey;
  private final long current;
  private final long limit;

  public QuotaExceededException(String quotaKey, long current, long limit) {
    super(
        String.format(
            "Quota exceeded for '%s': current=%d, limit=%d. Please upgrade your plan.",
            quotaKey, current, limit));
    this.quotaKey = quotaKey;
    this.current = current;
    this.limit = limit;
  }

  public String getQuotaKey() {
    return quotaKey;
  }

  public long getCurrent() {
    return current;
  }

  public long getLimit() {
    return limit;
  }
}
