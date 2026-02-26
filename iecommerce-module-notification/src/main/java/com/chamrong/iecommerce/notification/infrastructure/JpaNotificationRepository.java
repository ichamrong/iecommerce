package com.chamrong.iecommerce.notification.infrastructure;

import com.chamrong.iecommerce.notification.domain.Notification;
import com.chamrong.iecommerce.notification.domain.NotificationRepository;
import com.chamrong.iecommerce.notification.domain.NotificationStatus;
import com.chamrong.iecommerce.notification.domain.NotificationType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link NotificationRepository} port. */
@Repository
public interface JpaNotificationRepository
    extends JpaRepository<Notification, Long>, NotificationRepository {

  @Override
  List<Notification> findByRecipient(String recipient);

  @Override
  List<Notification> findByTenantId(String tenantId);

  @Override
  List<Notification> findByTenantIdAndStatus(String tenantId, NotificationStatus status);

  @Override
  List<Notification> findByRecipientAndTypeAndStatus(
      String recipient, NotificationType type, NotificationStatus status);
}
