package com.chamrong.iecommerce.common.security;

/**
 * Thrown when a tenant is not allowed to access a module or feature (vertical mode, disabled
 * module, quota exceeded, or feature flag off). Use {@link #getErrorCode()} for stable API
 * response.
 */
public class CapabilityDeniedException extends RuntimeException {

  /** Tenant's vertical or plan does not allow this module. */
  public static final String MODULE_DISABLED = "MODULE_DISABLED";

  /** Operation not allowed for tenant's vertical mode. */
  public static final String VERTICAL_NOT_ALLOWED = "VERTICAL_NOT_ALLOWED";

  /** Plan quota exceeded. */
  public static final String QUOTA_EXCEEDED = "QUOTA_EXCEEDED";

  /** Feature flag off for tenant. */
  public static final String FEATURE_DISABLED = "FEATURE_DISABLED";

  private final String errorCode;

  public CapabilityDeniedException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode != null ? errorCode : MODULE_DISABLED;
  }

  public CapabilityDeniedException(String errorCode) {
    this("Capability denied", errorCode);
  }

  public String getErrorCode() {
    return errorCode;
  }
}
