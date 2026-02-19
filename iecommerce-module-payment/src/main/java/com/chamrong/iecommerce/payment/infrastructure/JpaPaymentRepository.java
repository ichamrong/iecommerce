package com.chamrong.iecommerce.payment.infrastructure;

import com.chamrong.iecommerce.payment.domain.Payment;
import com.chamrong.iecommerce.payment.domain.PaymentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaPaymentRepository implements PaymentRepository {

  private final PaymentJpaInterface jpaInterface;

  public JpaPaymentRepository(PaymentJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public Payment save(Payment payment) {
    return jpaInterface.save(payment);
  }

  @Override
  public Optional<Payment> findById(Long id) {
    return jpaInterface.findById(id);
  }

  @Override
  public List<Payment> findByOrderId(Long orderId) {
    return jpaInterface.findByOrderId(orderId);
  }

  public interface PaymentJpaInterface extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
  }
}
