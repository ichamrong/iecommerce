package com.chamrong.iecommerce.report.domain.model;

/**
 * Supported logical report types across e-commerce, POS, accommodation, subscription, and
 * supporting modules.
 */
public enum ReportType {

  // ── Executive ───────────────────────────────────────────────────────────────
  BUSINESS_OVERVIEW,
  PROFIT_SNAPSHOT,

  // ── Sales / Order ───────────────────────────────────────────────────────────
  SALES_DAILY,
  SALES_BY_CHANNEL,
  SALES_BY_STORE,
  SALES_BY_TERMINAL,
  SALES_BY_STAFF,
  SALES_BY_SKU,
  SALES_BY_CATEGORY,
  ORDER_FUNNEL,
  ORDER_CANCELLATIONS,
  FULFILLMENT_SLA,
  INVOICE_STATUS,
  AR_AGING,

  // ── Payment / Finance ──────────────────────────────────────────────────────
  PAYMENT_METHOD_MIX,
  PAYMENT_SUCCESS_RATE,
  REFUNDS,
  CHARGEBACKS,
  FEES,
  TAX_SUMMARY,
  SETTLEMENT_SUMMARY,

  // ── Promotion ──────────────────────────────────────────────────────────────
  PROMOTION_PERFORMANCE,
  VOUCHER_REDEMPTIONS,
  DISCOUNT_CAP_HITS,
  PROMOTION_ABUSE_SIGNALS,

  // ── Inventory ──────────────────────────────────────────────────────────────
  STOCK_ON_HAND,
  STOCK_MOVEMENT,
  LOW_STOCK,
  STOCK_VALUATION,
  SHRINKAGE,
  OUT_OF_STOCK_IMPACT,

  // ── Catalog ────────────────────────────────────────────────────────────────
  CATALOG_HEALTH,
  PRICE_CHANGE_AUDIT,
  TOP_PRODUCTS,
  BOTTOM_PRODUCTS,
  CATEGORY_PERFORMANCE,

  // ── Customer ───────────────────────────────────────────────────────────────
  NEW_VS_RETURNING,
  CUSTOMER_LIFETIME_VALUE,
  COHORT_RETENTION,
  RFM_SEGMENTATION,
  TOP_CUSTOMERS,

  // ── Staff / POS ────────────────────────────────────────────────────────────
  CASHIER_PERFORMANCE,
  TERMINAL_REPORT,
  SHIFT_CLOSING,
  OPERATIONAL_EXCEPTIONS,

  // ── Booking (Accommodation) ────────────────────────────────────────────────
  OCCUPANCY,
  ADR,
  REVPAR,
  LENGTH_OF_STAY,
  BOOKING_LEAD_TIME,
  BOOKING_CANCELLATIONS,
  NIGHTLY_REVENUE,
  NO_SHOW_REPORT,

  // ── Subscription ───────────────────────────────────────────────────────────
  MRR,
  ARR,
  ACTIVE_SUBSCRIPTIONS,
  CHURN,
  PLAN_MIX,
  DUNNING_REPORT,

  // ── Reviews ────────────────────────────────────────────────────────────────
  RATING_SUMMARY,
  REVIEW_VOLUME_TREND,
  REVIEW_MODERATION,

  // ── Notification ───────────────────────────────────────────────────────────
  DELIVERY_METRICS,
  FAILED_DELIVERY,
  CAMPAIGN_PERFORMANCE,

  // ── Security / Audit ──────────────────────────────────────────────────────
  LOGIN_ACTIVITY,
  ROLE_PERMISSION_CHANGES,
  AUDIT_TRAIL,
  CONFIGURATION_CHANGES,

  // ── Asset ──────────────────────────────────────────────────────────────────
  ASSET_USAGE,
  PUBLIC_ASSET_AUDIT
}
