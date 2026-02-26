package com.chamrong.iecommerce.chat.application.dto;

import java.time.Instant;

public record ChatMessageResponse(
    Long id, Long conversationId, Long senderId, String content, boolean read, Instant timestamp) {}
