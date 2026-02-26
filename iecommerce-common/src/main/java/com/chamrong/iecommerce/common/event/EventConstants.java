package com.chamrong.iecommerce.common.event;

public final class EventConstants {

  private EventConstants() {}

  // Order Events
  public static final String ORDER_CREATED = "OrderCreatedEvent";
  public static final String ORDER_CONFIRMED = "OrderConfirmedEvent";
  public static final String ORDER_SHIPPED = "OrderShippedEvent";
  public static final String ORDER_COMPLETED = "OrderCompletedEvent";
  public static final String ORDER_CANCELLED = "OrderCancelledEvent";

  // Inventory Events
  public static final String STOCK_RESERVED = "StockReservedEvent";
  public static final String INVENTORY_OPERATION_FAILED = "InventoryOperationFailedEvent";

  // Payment Events
  public static final String PAYMENT_SUCCEEDED = "PaymentSucceededEvent";
  public static final String PAYMENT_FAILED = "PaymentFailedEvent";

  // Customer Events
  public static final String CUSTOMER_CREATED = "CustomerCreatedEvent";
  public static final String CUSTOMER_BLOCKED = "CustomerBlockedEvent";
  public static final String CUSTOMER_UNBLOCKED = "CustomerUnblockedEvent";
}
