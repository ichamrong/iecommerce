package com.chamrong.iecommerce.notification.application;

import com.chamrong.iecommerce.notification.domain.Notification;
import com.chamrong.iecommerce.notification.domain.NotificationRepository;
import com.chamrong.iecommerce.notification.domain.NotificationStatus;
import com.chamrong.iecommerce.notification.domain.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

  private final NotificationRepository notificationRepository;

  public NotificationService(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  @Transactional
  public void sendEmail(String to, String subject, String body) {
    Notification notification = new Notification();
    notification.setRecipient(to);
    notification.setSubject(subject);
    notification.setContent(body);
    notification.setType(NotificationType.EMAIL);
    notification.setStatus(NotificationStatus.PENDING);

    notificationRepository.save(notification);

    // Logical "send" happens here or via a dedicated provider
  }
}
