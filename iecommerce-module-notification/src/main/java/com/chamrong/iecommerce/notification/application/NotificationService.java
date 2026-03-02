package com.chamrong.iecommerce.notification.application;

import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.notification.NotificationApi;
import com.chamrong.iecommerce.notification.application.dto.NotificationRequest;
import com.chamrong.iecommerce.notification.application.dto.NotificationResponse;
import com.chamrong.iecommerce.notification.application.spi.SmsProvider;
import com.chamrong.iecommerce.notification.application.spi.TelegramProvider;
import com.chamrong.iecommerce.notification.domain.Notification;
import com.chamrong.iecommerce.notification.domain.NotificationRepository;
import com.chamrong.iecommerce.notification.domain.NotificationStatus;
import com.chamrong.iecommerce.notification.domain.NotificationTemplateRepository;
import com.chamrong.iecommerce.notification.domain.NotificationType;
import com.chamrong.iecommerce.notification.infrastructure.NotificationKeysetQuery;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationApi {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;
  private static final String ENDPOINT_LIST_NOTIFICATIONS = "notification:list";

  private final NotificationRepository notificationRepository;
  private final NotificationTemplateRepository templateRepository;
  private final TemplateEngine templateEngine;
  private final JavaMailSender mailSender;
  private final List<SmsProvider> smsProviders;
  private final TelegramProvider telegramProvider;
  private final NotificationKeysetQuery keysetQuery;

  @Value("${notification.sms.provider:twilio}")
  private String activeSmsProvider;

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

  @Override
  @Transactional
  public void sendTemplatedNotification(
      String tenantId,
      String recipient,
      String templateKey,
      String locale,
      java.util.Map<String, Object> data) {

    // 1. Find templates for all supported types or a specific preferred one
    // For simplicity, we try to send standard channels: Email, SMS, In-App, Push
    for (NotificationType type :
        java.util.List.of(
            NotificationType.EMAIL,
            NotificationType.SMS,
            NotificationType.IN_APP,
            NotificationType.PUSH)) {
      templateRepository
          .findByTenantIdAndTemplateKeyAndTypeAndLocale(tenantId, templateKey, type, locale)
          .ifPresent(
              template -> {
                String subject = templateEngine.render(template.getSubjectTemplate(), data);
                String content = templateEngine.render(template.getContentTemplate(), data);

                NotificationRequest req = new NotificationRequest(recipient, subject, content);
                dispatch(tenantId, req, type);
              });
    }
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

  @Transactional
  public NotificationResponse sendTelegram(String tenantId, NotificationRequest req) {
    return dispatch(tenantId, req, NotificationType.TELEGRAM);
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
    return notificationRepository.findByTenantId(tenantId).stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> getFailed(String tenantId) {
    return notificationRepository
        .findByTenantIdAndStatus(tenantId, NotificationStatus.FAILED)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> getActiveInApp(String recipient) {
    return notificationRepository
        .findByRecipientAndTypeAndStatus(
            recipient, NotificationType.IN_APP, NotificationStatus.SENT)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  // ── Internal ───────────────────────────────────────────────────────────────

  /**
   * Cursor-paginated notifications for a tenant, sorted by created_at DESC, id DESC.
   *
   * @param tenantId current tenant
   * @param cursor opaque cursor; null/blank = first page
   * @param limit requested page size (1–100)
   */
  @Transactional(readOnly = true)
  public CursorPageResponse<NotificationResponse> listByTenantCursor(
      String tenantId, String cursor, int limit) {
    int effectiveLimit = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
    int fetchLimit = effectiveLimit + 1;

    Map<String, Object> filterMap = new LinkedHashMap<>();
    filterMap.put("tenantId", tenantId);
    String filterHash = FilterHasher.computeHash(ENDPOINT_LIST_NOTIFICATIONS, filterMap);

    Instant afterCreatedAt = null;
    Long afterId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      afterCreatedAt = payload.getCreatedAt();
      try {
        afterId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new InvalidCursorException(
            InvalidCursorException.INVALID_CURSOR, "Invalid cursor id");
      }
    }

    List<Notification> items =
        keysetQuery.findNextPage(tenantId, afterCreatedAt, afterId, fetchLimit);

    boolean hasNext = items.size() > effectiveLimit;
    List<Notification> page = hasNext ? items.subList(0, effectiveLimit) : items;

    List<NotificationResponse> data = page.stream().map(this::toResponse).toList();

    String nextCursor = null;
    if (hasNext && !page.isEmpty()) {
      Notification last = page.get(page.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), String.valueOf(last.getId()), filterHash));
    }

    return CursorPageResponse.of(data, nextCursor, hasNext, effectiveLimit);
  }

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
      switch (type) {
        case EMAIL -> {
          SimpleMailMessage message = new SimpleMailMessage();
          message.setTo(req.recipient());
          message.setSubject(req.subject());
          message.setText(req.content());
          mailSender.send(message);
          log.info("Dispatching EMAIL to {} [tenant={}]", req.recipient(), tenantId);
        }
        case SMS -> {
          var provider =
              smsProviders.stream()
                  .filter(p -> p.supports(activeSmsProvider))
                  .findFirst()
                  .orElseThrow(
                      () ->
                          new IllegalStateException(
                              "No SMS provider found for: " + activeSmsProvider));
          provider.sendSms(req.recipient(), req.content());
          log.info(
              "Dispatching SMS to {} [tenant={}] via {}",
              req.recipient(),
              tenantId,
              activeSmsProvider);
        }
        case TELEGRAM -> {
          telegramProvider.sendMessage(req.recipient(), req.content());
          log.info("Dispatching TELEGRAM to {} [tenant={}]", req.recipient(), tenantId);
        }
        default ->
            log.info(
                "Recording/Mock dispatching {} for {} [tenant={}]",
                type,
                req.recipient(),
                tenantId);
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
