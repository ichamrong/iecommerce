package com.chamrong.iecommerce.customer.infrastructure;

import com.chamrong.iecommerce.common.event.OrderCompletedEvent;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerSagaListener {

  private final CustomerRepository customerRepository;

  @EventListener
  @Transactional
  public void onOrderCompleted(OrderCompletedEvent event) {
    if (event.customerId() == null) return;

    log.info(
        "Saga [Customer]: Awarding {} points to customer {} for order {}",
        event.pointsEarned(),
        event.customerId(),
        event.orderId());

    customerRepository
        .findById(event.customerId())
        .ifPresent(
            customer -> {
              customer.addPoints(event.pointsEarned());
              customerRepository.save(customer);
              log.info(
                  "Saga [Customer]: Customer {} now has {} points (Tier: {})",
                  customer.getId(),
                  customer.getLoyaltyPoints(),
                  customer.getLoyaltyTier());
            });
  }
}
