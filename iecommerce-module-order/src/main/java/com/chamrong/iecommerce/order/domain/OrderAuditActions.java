package com.chamrong.iecommerce.order.domain;

/**
 * All audit action constants for the Order module.
 *
 * <p>Using string constants avoids magic strings scattered across service classes, makes it easy to
 * add new actions in one place, and makes grep/search trivial.
 */
public final class OrderAuditActions {

  private OrderAuditActions() {} // prevent instantiation — utility class pattern

  public static final String ORDER_CREATED = "ORDER_CREATED";
  public static final String ITEM_ADDED = "ITEM_ADDED";
  public static final String VOUCHER_APPLIED = "VOUCHER_APPLIED";
  public static final String ORDER_CONFIRMED = "ORDER_CONFIRMED";
  public static final String ORDER_ARRANGING_PAYMENT = "ORDER_ARRANGING_PAYMENT";
  public static final String ORDER_PAYMENT_AUTHORIZED = "ORDER_PAYMENT_AUTHORIZED";
  public static final String ORDER_PICKING = "ORDER_PICKING";
  public static final String ORDER_PACKING = "ORDER_PACKING";
  public static final String ORDER_SHIPPED = "ORDER_SHIPPED";
  public static final String ORDER_DELIVERED = "ORDER_DELIVERED";
  public static final String ORDER_COMPLETED = "ORDER_COMPLETED";
  public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
  public static final String IDOR_ATTEMPT = "IDOR_ATTEMPT";
}
