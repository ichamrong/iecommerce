package com.chamrong.iecommerce.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notification_templates")
@Getter
@Setter
public class NotificationTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String tenantId;

  private String templateKey;

  @Enumerated(EnumType.STRING)
  private NotificationType type;

  private String locale; // e.g., km-KH, en-US

  private String subjectTemplate;

  @Column(columnDefinition = "TEXT")
  private String contentTemplate;
}
