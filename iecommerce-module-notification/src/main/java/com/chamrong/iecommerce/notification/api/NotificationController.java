package com.chamrong.iecommerce.notification.api;

import com.chamrong.iecommerce.notification.application.NotificationService;
import com.chamrong.iecommerce.notification.application.dto.NotificationRequest;
import com.chamrong.iecommerce.notification.application.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notification dispatch and history.
 *
 * <p>Base path: {@code /api/v1/notifications}
 */
@Tag(name = "Notifications", description = "Email, SMS, and push notification dispatch")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('notifications:manage')")
public class NotificationController {

  private final NotificationService notificationService;

  @Operation(summary = "Send an email notification")
  @PostMapping("/email")
  public ResponseEntity<NotificationResponse> sendEmail(
      @RequestParam String tenantId, @RequestBody NotificationRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(notificationService.sendEmail(tenantId, req));
  }

  @Operation(summary = "Send an SMS notification")
  @PostMapping("/sms")
  public ResponseEntity<NotificationResponse> sendSms(
      @RequestParam String tenantId, @RequestBody NotificationRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(notificationService.sendSms(tenantId, req));
  }

  @Operation(summary = "Send a push notification")
  @PostMapping("/push")
  public ResponseEntity<NotificationResponse> sendPush(
      @RequestParam String tenantId, @RequestBody NotificationRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(notificationService.sendPush(tenantId, req));
  }

  @Operation(summary = "Get notification history for a tenant")
  @GetMapping
  public List<NotificationResponse> listByTenant(@RequestParam String tenantId) {
    return notificationService.getByTenant(tenantId);
  }

  @Operation(
      summary = "Get failed notifications",
      description = "Returns notifications that failed to dispatch — useful for retry dashboards.")
  @GetMapping("/failed")
  public List<NotificationResponse> getFailed(@RequestParam String tenantId) {
    return notificationService.getFailed(tenantId);
  }
}
