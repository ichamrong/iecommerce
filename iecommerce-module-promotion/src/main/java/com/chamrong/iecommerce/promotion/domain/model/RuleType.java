package com.chamrong.iecommerce.promotion.domain.model;

/** Supported types of eligibility rules. */
public enum RuleType {
  // Retail
  PRODUCT_IN_LIST,
  CATEGORY_MATCH,
  MIN_PURCHASE_QUANTITY,

  // Hospitality
  MIN_NIGHTS_STAY,
  EARLY_BIRD_DAYS,

  // F&B / General
  TIME_OF_DAY_RANGE,
  DAY_OF_WEEK,

  // Geographic / Logistic
  REGION_MATCH,
  DELIVERY_ZONE,
  STORE_PICKUP_ONLY,

  // Customer
  CUSTOMER_TIER_MATCH,
  FIRST_ORDER_ONLY
}
