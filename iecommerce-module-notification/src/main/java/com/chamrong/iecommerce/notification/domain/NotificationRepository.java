package com.chamrong.iecommerce.notification.domain;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
  Notification save(Notification notification);

  Optional<Notification> findById(Long id);

  List<Notification> findByRecipient(String recipient);

  List<Notification> findByTenantId(String tenantId);

  List<Notification> findByTenantIdAndStatus(String tenantId, NotificationStatus status);

  List<Notification> findByRecipientAndTypeAndStatus(
      String recipient, NotificationType type, NotificationStatus status);
}
