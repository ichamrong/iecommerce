package com.chamrong.iecommerce.customer.domain.model;

/** Lifecycle status of a customer account. LOCKED for auth lockout; DELETED for soft delete. */
public enum CustomerStatus {
  ACTIVE,
  LOCKED,
  BLOCKED,
  DELETED
}
