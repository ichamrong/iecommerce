package com.chamrong.iecommerce.audit.domain.model;

/**
 * Stable event type codes for audit events.
 *
 * <p>Use consistent codes (e.g. ORDER_CONFIRM, PAYMENT_CAPTURE) for filtering and reporting. Extend
 * as needed per module; avoid free-form strings in API.
 */
public enum AuditEventType {

  // Catalog
  PRODUCT_CREATE,
  PRODUCT_UPDATE,
  PRODUCT_PUBLISH,
  PRODUCT_ARCHIVE,
  PRODUCT_DELETE,
  CATEGORY_CREATE,
  CATEGORY_UPDATE,
  CATEGORY_DELETE,
  COLLECTION_CREATE,
  COLLECTION_UPDATE,
  FACET_CREATE,
  FACET_VALUE_ADD,
  FACET_VALUE_REMOVE,

  // Auth
  USER_REGISTER,
  USER_LOGIN,
  USER_LOGIN_FAILED,
  USER_DISABLE,
  TENANT_REGISTER,
  TENANT_STATUS_UPDATE,
  TENANT_PREFERENCES_UPDATE,

  // Customer
  CUSTOMER_CREATE,
  CUSTOMER_UPDATE,
  CUSTOMER_BLOCK,
  CUSTOMER_UNBLOCK,
  CUSTOMER_ADDRESS_ADD,
  CUSTOMER_ADDRESS_UPDATE,
  CUSTOMER_ADDRESS_REMOVE,

  // Staff
  STAFF_CREATE,
  STAFF_SUSPEND,
  STAFF_TERMINATE,
  STAFF_REACTIVATE,

  // Order & Booking
  ORDER_COMPLETE,
  BOOKING_CONFIRM,

  // Payment
  PAYMENT_SUCCEED,
  PAYMENT_FAIL,

  // Storage
  STORAGE_UPLOAD,
  STORAGE_DOWNLOAD,
  STORAGE_DELETE,

  // Generic (when no enum matches)
  CUSTOM
}
