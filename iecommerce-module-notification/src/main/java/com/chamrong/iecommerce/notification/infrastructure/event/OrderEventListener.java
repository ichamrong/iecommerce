package com.chamrong.iecommerce.notification.infrastructure.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.chamrong.iecommerce.common.event.OrderCancelledEvent;
import com.chamrong.iecommerce.common.event.OrderCompletedEvent;
import com.chamrong.iecommerce.common.event.OrderConfirmedEvent;
import com.chamrong.iecommerce.common.event.OrderShippedEvent;
import com.chamrong.iecommerce.customer.CustomerApi;
import com.chamrong.iecommerce.notification.NotificationApi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

  private final CustomerApi customerApi;
  private final NotificationApi notificationApi;

  @EventListener
  public void onOrderConfirmed(OrderConfirmedEvent event) {
    log.info("Handling order confirmation for orderId={}", event.orderId());

    customerApi.getCustomer(event.customerId()).ifPresent(customer -> {
      String subject = "Order Confirmed: #" + event.orderId();
      String content = "Hi " + customer.firstName() + ",\n\n" +
          "Your order #" + event.orderId() + " has been confirmed and is being processed.\n" +
          "We'll notify you when it ships!";

      notificationApi.sendNotification(event.tenantId(), customer.email(), subject, content);
    });
  }

  @EventListener
  public void onOrderShipped(OrderShippedEvent event) {
    log.info("Handling order shipped for orderId={}", event.orderId());

    customerApi.getCustomer(event.customerId()).ifPresent(customer -> {
      String subject = "Order Shipped: #" + event.orderId();
      String content = "Great news, " + customer.firstName() + "!\n\n" +
          "Your order #" + event.orderId() + " has been shipped.\n" +
          "Tracking Number: " + (event.trackingNumber() != null ? event.trackingNumber() : "N/A") + "\n\n" +
          "Thank you for shopping with us!";

      notificationApi.sendNotification(event.tenantId(), customer.email(), subject, content);
    });
  }

  @EventListener
  public void onOrderCancelled(OrderCancelledEvent event) {
    log.info("Handling order cancellation for orderId={}", event.orderId());

    customerApi.getCustomer(event.customerId()).ifPresent(customer -> {
      String subject = "Order Cancelled: #" + event.orderId();
      String content = "Hi " + customer.firstName() + ",\n\n" +
          "Your order #" + event.orderId() + " has been cancelled.\n" +
          "If you have any questions, please contact support.";

      notificationApi.sendNotification(event.tenantId(), customer.email(), subject, content);
    });
  }

  @EventListener
  public void onOrderCompleted(OrderCompletedEvent event) {
    log.info("Handling order completion for orderId={}", event.orderId());

    customerApi.getCustomer(event.customerId()).ifPresent(customer -> {
      String subject = "How was your order? #" + event.orderId();
      String content = "Hi " + customer.firstName() + ",\n\n" +
          "Your order #" + event.orderId() + " is now complete.\n" +
          "You've earned " + event.pointsEarned() + " loyalty points!\n\n" +
          "We'd love to hear your feedback. Please leave a review!";

      notificationApi.sendNotification(event.tenantId(), customer.email(), subject, content);
    });
  }
}
