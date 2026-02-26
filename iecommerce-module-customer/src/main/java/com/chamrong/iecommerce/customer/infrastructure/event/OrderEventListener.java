package com.chamrong.iecommerce.customer.infrastructure.event;

import com.chamrong.iecommerce.common.event.OrderCompletedEvent;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderEventListener {

  private final CustomerRepository customerRepository;

  public OrderEventListener(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @EventListener
  @Transactional
  public void onOrderCompleted(OrderCompletedEvent event) {
    if (event.customerId() != null) {
      customerRepository
          .findById(event.customerId())
          .ifPresent(
              customer -> {
                if (customer.getTenantId().equals(event.tenantId())) {
                  customer.addPoints(event.pointsEarned());
                  customerRepository.save(customer);
                }
              });
    }
  }
}
