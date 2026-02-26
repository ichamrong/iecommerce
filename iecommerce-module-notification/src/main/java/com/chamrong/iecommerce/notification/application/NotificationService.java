package com.chamrong.iecommerce.notification.application;

import com.chamrong.iecommerce.notification.application.dto.NotificationRequest;
import com.chamrong.iecommerce.notification.application.dto.NotificationResponse;
import com.chamrong.iecommerce.notification.domain.Notification;
import com.chamrong.iecommerce.notification.domain.NotificationRepository;
import com.chamrong.iecommerce.notification.domain.NotificationStatus;
import com.chamrong.iecommerce.notification.domain.NotificationType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.chamrong.iecommerce.notification.infrastructure.twilio.TwilioConfiguration;

/**
 * Notification service — persists outbound messages and simulates dispatch.
 *
 * <p>In production, {@code dispatch()} would delegate to an SMTP client, Firebase, Twilio, etc.
 * For now it records the message and marks it SENT to keep the system functional without external
 * dependencies.
 */
import com.chamrong.iecommerce.notification.NotificationApi;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationApi {

  private final NotificationRepository notificationRepository;
  private final JavaMailSender mailSender;
  private final TwilioConfiguration twilioConfiguration;

  @Override
  @Transactional
  public void sendNotification(String tenantId, String recipient, String subject, String content) {
    Notification n = new Notification();
    n.setTenantId(tenantId);
    n.setRecipient(recipient);
    n.setSubject(subject);
    n.setContent(content);
    n.setType(NotificationType.EMAIL);

    // Auto-dispatch (simulation)
    n.markSent();
    notificationRepository.save(n);
    log.info("Notification stored: to={} subject={}", recipient, subject);
  }

  // ── Commands ───────────────────────────────────────────────────────────────

  @Transactional
  public NotificationResponse sendEmail(String tenantId, NotificationRequest req) {
    return dispatch(tenantId, req, NotificationType.EMAIL);
  }

  @Transactional
  public NotificationResponse sendSms(String tenantId, NotificationRequest req) {
    return dispatch(tenantId, req, NotificationType.SMS);
  }

  @Transactional
  public NotificationResponse sendPush(String tenantId, NotificationRequest req) {
    return dispatch(tenantId, req, NotificationType.PUSH);
  }

  // ── Queries ────────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public List<NotificationResponse> getByRecipient(String recipient) {
    return notificationRepository.findByRecipient(recipient).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> getByTenant(String tenantId) {
    return notificationRepository.findByTenantId(tenantId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> getFailed(String tenantId) {
    return notificationRepository
        .findByTenantIdAndStatus(tenantId, NotificationStatus.FAILED)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  // ── Internal ───────────────────────────────────────────────────────────────

  private NotificationResponse dispatch(
      String tenantId, NotificationRequest req, NotificationType type) {
    Notification n = new Notification();
    n.setTenantId(tenantId);
    n.setRecipient(req.recipient());
    n.setSubject(req.subject() != null ? req.subject() : "");
    n.setContent(req.content());
    n.setType(type);
    n.setStatus(NotificationStatus.PENDING);
    notificationRepository.save(n);

    try {
      if (type == NotificationType.EMAIL) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(req.recipient());
        message.setSubject(req.subject());
        message.setText(req.content());
        mailSender.send(message);
        log.info("Dispatching EMAIL to {} [tenant={}]", req.recipient(), tenantId);
      } else if (type == NotificationType.SMS) {
        if (!"dummy_sid".equals(twilioConfiguration.getAccountSid())) {
          Message.creator(
              new PhoneNumber(req.recipient()),
              new PhoneNumber(twilioConfiguration.getSystemPhoneNumber()),
              req.content()
          ).create();
          log.info("Dispatching real SMS to {} [tenant={}] via Twilio", req.recipient(), tenantId);
        } else {
          log.info("Mock dispatching SMS to {} [tenant={}]", req.recipient(), tenantId);
        }
      } else {
        log.info("Mock dispatching {} to {} [tenant={}]", type, req.recipient(), tenantId);
      }
      n.markSent();
    } catch (Exception ex) {
      log.error("Failed to dispatch {} to {}: {}", type, req.recipient(), ex.getMessage());
      n.markFailed(ex.getMessage());
    }

    return toResponse(notificationRepository.save(n));
  }

  private NotificationResponse toResponse(Notification n) {
    return new NotificationResponse(
        n.getId(),
        n.getRecipient(),
        n.getSubject(),
        n.getType().name(),
        n.getStatus().name(),
        n.getErrorMessage(),
        n.getCreatedAt());
  }
}
