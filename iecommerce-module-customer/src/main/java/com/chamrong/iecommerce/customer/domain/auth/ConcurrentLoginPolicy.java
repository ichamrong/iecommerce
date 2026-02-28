package com.chamrong.iecommerce.customer.domain.auth;

public enum ConcurrentLoginPolicy {
  INVALIDATE_OLD,
  ALLOW_MULTIPLE, // Default state for backward compatibility if configured
  FORCE_LOGOUT_NEW
}
