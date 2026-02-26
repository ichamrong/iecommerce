package com.chamrong.iecommerce.notification.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notification_log")
public class Notification extends BaseTenantEntity {

  @Column(nullable = false, length = 255)
  private String recipient;

  @Column(nullable = false, length = 255)
  private String subject;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private NotificationType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private NotificationStatus status = NotificationStatus.PENDING;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  // ── Domain behaviour ───────────────────────────────────────────────────────

  public void markSent() {
    this.status = NotificationStatus.SENT;
    this.errorMessage = null;
  }

  public void markFailed(String reason) {
    this.status = NotificationStatus.FAILED;
    this.errorMessage = reason;
  }
}
