package com.chamrong.iecommerce.order.infrastructure;

import com.chamrong.iecommerce.booking.BookingConfirmedEvent;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderAuditActions;
import com.chamrong.iecommerce.order.domain.OrderItem;
import com.chamrong.iecommerce.order.domain.OrderState;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Automatically creates an Order record when a Booking is confirmed. This ensures the financial
 * side (payment, invoice) can be handled by the Order module.
 */
@Slf4j
@Component("orderBookingEventListener")
@RequiredArgsConstructor
public class BookingEventListener {

  private final OrderRepositoryPort orderRepository;
  private final OrderAuditPort auditLogRepository;

  @EventListener
  @Transactional
  public void onBookingConfirmed(BookingConfirmedEvent event) {
    log.info(
        "Received BookingConfirmedEvent for bookingId={}. Creating order...", event.bookingId());

    Order order = new Order();
    order.assignTenantId(event.tenantId());
    order.setCustomerId(event.customerId());
    order.assignCode("BOOK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

    order.addItem(
        OrderItem.of(
            event.resourceVariantId(), 1, event.totalPrice(), event.startAt(), event.endAt()));

    // Let the domain logic calculate totals to prevent mismatch bugs
    order.recalculateTotals();

    // The order starts in AddingItems. Transition it to Confirmed.
    order.confirm();

    Order saved = orderRepository.save(order);

    // Banking-grade audit log for traceability
    auditLogRepository.log(
        saved.getId(),
        saved.getTenantId(),
        OrderState.AddingItems,
        OrderState.Confirmed,
        OrderAuditActions.ORDER_CREATED,
        "system",
        "bookingId=" + event.bookingId());

    log.info("Order created successfully id={} for bookingId={}", saved.getId(), event.bookingId());
  }
}
