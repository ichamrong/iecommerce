package com.chamrong.iecommerce.order.infrastructure;

import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link OrderRepository} port. */
@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long>, OrderRepository {
  @Override
  Optional<Order> findByCode(String code);
}
