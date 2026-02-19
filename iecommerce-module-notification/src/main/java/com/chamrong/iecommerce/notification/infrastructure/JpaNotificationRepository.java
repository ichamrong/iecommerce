package com.chamrong.iecommerce.notification.infrastructure;

import com.chamrong.iecommerce.notification.domain.Notification;
import com.chamrong.iecommerce.notification.domain.NotificationRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaNotificationRepository implements NotificationRepository {

  private final NotificationJpaInterface jpaInterface;

  public JpaNotificationRepository(NotificationJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public Notification save(Notification notification) {
    return jpaInterface.save(notification);
  }

  @Override
  public Optional<Notification> findById(Long id) {
    return jpaInterface.findById(id);
  }

  @Override
  public List<Notification> findByRecipient(String recipient) {
    return jpaInterface.findByRecipient(recipient);
  }

  public interface NotificationJpaInterface extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipient(String recipient);
  }
}
