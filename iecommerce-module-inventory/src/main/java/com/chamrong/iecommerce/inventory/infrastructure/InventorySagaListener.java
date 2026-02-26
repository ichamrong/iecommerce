package com.chamrong.iecommerce.inventory.infrastructure;

import com.chamrong.iecommerce.inventory.application.InventoryService;
import com.chamrong.iecommerce.common.event.InventoryOperationFailedEvent;
import com.chamrong.iecommerce.common.event.OrderCancelledEvent;
import com.chamrong.iecommerce.common.event.OrderConfirmedEvent;
import com.chamrong.iecommerce.common.event.OrderShippedEvent;
import com.chamrong.iecommerce.common.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saga Listener for the Inventory module. 
 * Reacts to Order events published via the Outbox pattern.
 *
 * <p><b>Banking/Insurance standard:</b>
 * This ensures that if the Order module successfully commits, the Inventory operations 
 * will follow eventually (at-least-once). If inventory fails (e.g. OutOfStock), 
 * a compensating transaction should be triggered (not implemented yet, would involve 
 * publishing an 'InventoryReservationFailedEvent' back to the Order module).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventorySagaListener {

  private final InventoryService inventoryService;

  @EventListener
  @Transactional
  public void onOrderConfirmed(OrderConfirmedEvent event) {
    log.info("Saga [Inventory]: Reserving stock for order {} (tenant: {})", 
        event.orderId(), event.tenantId());
    
    try {
      event.items().forEach(item -> 
          inventoryService.reserveStock(event.tenantId(), item.productVariantId(), item.quantity())
      );
      
      var reservedItems = event.items().stream()
          .map(i -> new StockReservedEvent.Item(i.productVariantId(), i.quantity()))
          .toList();
          
      inventoryService.saveOutbox(event.tenantId(), "StockReservedEvent", 
          new StockReservedEvent(event.orderId(), event.tenantId(), reservedItems));
          
    } catch (Exception ex) {
      log.error("Saga [Inventory]: Failed to reserve stock for order {}. Emitting failure event.", event.orderId(), ex);
      inventoryService.saveOutbox(event.tenantId(), "InventoryOperationFailedEvent",
          new InventoryOperationFailedEvent(event.orderId(), event.tenantId(), ex.getMessage()));
    }
  }

  @EventListener
  @Transactional
  public void onOrderCancelled(OrderCancelledEvent event) {
    log.info("Saga [Inventory]: Releasing stock for order {} (tenant: {})", 
        event.orderId(), event.tenantId());
    
    event.items().forEach(item -> 
        inventoryService.releaseStock(event.tenantId(), item.productVariantId(), item.quantity())
    );
  }

  @EventListener
  @Transactional
  public void onOrderShipped(OrderShippedEvent event) {
    log.info("Saga [Inventory]: Deducting stock permanently for order {} (tenant: {})", 
        event.orderId(), event.tenantId());
    
    event.items().forEach(item -> 
        inventoryService.deductStock(event.tenantId(), item.productVariantId(), item.quantity())
    );
  }
}
