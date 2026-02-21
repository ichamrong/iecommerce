package com.chamrong.iecommerce.auth.domain;

public enum TenantStatus {
  /** Self-service signup — awaiting email verification. */
  PENDING_VERIFICATION,

  /** Active on a limited-time Free Trial. */
  TRIAL,

  /** Fully operational tenant with a paid or active subscription. */
  ACTIVE,

  /** Temporarily suspended or manually disabled due to unpaid bills or expiration. */
  DISABLED
}
