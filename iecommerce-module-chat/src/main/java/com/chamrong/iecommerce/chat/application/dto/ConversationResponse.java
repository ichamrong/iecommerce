package com.chamrong.iecommerce.chat.application.dto;

import java.time.Instant;
import java.util.Set;

public record ConversationResponse(
    Long id,
    Set<Long> participantIds,
    Instant lastMessageTimestamp,
    Instant createdAt) {}
