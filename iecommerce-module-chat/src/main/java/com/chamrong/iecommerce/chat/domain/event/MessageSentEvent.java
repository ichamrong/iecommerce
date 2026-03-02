package com.chamrong.iecommerce.chat.domain.event;

import java.time.Instant;

/** Domain event when a message is sent. */
public record MessageSentEvent(
    String tenantId, Long conversationId, Long messageId, Long senderId, Instant occurredAt) {

  public MessageSentEvent(String tenantId, Long conversationId, Long messageId, Long senderId) {
    this(tenantId, conversationId, messageId, senderId, Instant.now());
  }
}
