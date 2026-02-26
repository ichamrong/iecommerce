package com.chamrong.iecommerce.notification.application.dto;

import java.time.Instant;

public record NotificationResponse(
    Long id,
    String recipient,
    String subject,
    String type,
    String status,
    String errorMessage,
    Instant createdAt) {}
