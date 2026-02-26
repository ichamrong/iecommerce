package com.chamrong.iecommerce.order.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chamrong.iecommerce.common.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderStateTest {

  private Order order;

  @BeforeEach
  void setUp() {
    order = new Order();
    order.assignCode("TEST-123");
    order.assignTenantId("tenant-1");

    OrderItem item = new OrderItem();
    item.setQuantity(1);
    item.setUnitPrice(Money.of(BigDecimal.TEN, "USD"));
    order.addItem(item);
  }

  @Test
  void testInitialState() {
    assertEquals(OrderState.AddingItems, order.getState());
  }

  @Test
  void testValidStateTransitions() {
    // Initial: AddingItems -> Confirmed
    order.confirm();
    assertEquals(OrderState.Confirmed, order.getState());

    // Confirmed -> Picking
    order.pick();
    assertEquals(OrderState.Picking, order.getState());

    // Picking -> Packing
    order.pack();
    assertEquals(OrderState.Packing, order.getState());

    // Packing -> Shipped
    order.ship("TRACK-123");
    assertEquals(OrderState.Shipped, order.getState());

    // Shipped -> Delivered
    order.deliver();
    assertEquals(OrderState.Delivered, order.getState());

    // Delivered -> Completed
    order.complete();
    assertEquals(OrderState.Completed, order.getState());
  }

  @Test
  void testCancelFromValidState() {
    order.confirm();
    order.cancel();
    assertEquals(OrderState.Cancelled, order.getState());
  }

  @Test
  void testCancelFromCompletedThrowsException() {
    order.confirm();
    order.pick();
    order.pack();
    order.ship("TRACK-123");
    order.deliver();
    order.complete();

    assertThrows(OrderTransitionException.class, () -> order.cancel());
  }

  @Test
  void testInvalidTransitionThrowsException() {
    // Trying to ship from AddingItems
    assertThrows(OrderTransitionException.class, () -> order.ship("123"));
  }
}
