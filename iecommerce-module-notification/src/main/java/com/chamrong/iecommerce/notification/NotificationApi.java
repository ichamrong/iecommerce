package com.chamrong.iecommerce.notification;

/** Public API of the Notification module. */
public interface NotificationApi {

  /** Sends a simple notification (email/SMS depending on implementation). */
  void sendNotification(String tenantId, String recipient, String subject, String content);

  void sendTemplatedNotification(
      String tenantId,
      String recipient,
      String templateKey,
      String locale,
      java.util.Map<String, Object> data);
}
