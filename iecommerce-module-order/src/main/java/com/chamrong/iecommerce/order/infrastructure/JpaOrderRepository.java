package com.chamrong.iecommerce.order.infrastructure;

import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaOrderRepository implements OrderRepository {

  private final OrderJpaInterface jpaInterface;

  public JpaOrderRepository(OrderJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public Optional<Order> findById(Long id) {
    return jpaInterface.findById(id);
  }

  @Override
  public Optional<Order> findByCode(String code) {
    return jpaInterface.findByCode(code);
  }

  @Override
  public Order save(Order order) {
    return jpaInterface.save(order);
  }

  public interface OrderJpaInterface extends JpaRepository<Order, Long> {
    Optional<Order> findByCode(String code);
  }
}
