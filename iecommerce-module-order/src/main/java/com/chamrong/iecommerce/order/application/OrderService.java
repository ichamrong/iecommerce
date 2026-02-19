package com.chamrong.iecommerce.order.application;

import com.chamrong.iecommerce.order.OrderApi;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderRepository;
import com.chamrong.iecommerce.order.domain.OrderState;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService implements OrderApi {

  private final OrderRepository orderRepository;

  public OrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Transactional
  public Order createOrder() {
    Order order = new Order();
    order.setCode(UUID.randomUUID().toString());
    order.setState(OrderState.AddingItems);
    return orderRepository.save(order);
  }

  public Optional<Order> getOrder(Long id) {
    return orderRepository.findById(id);
  }
}
