package com.chamrong.iecommerce.chat.domain.event;

import com.chamrong.iecommerce.chat.domain.model.ConversationType;
import java.time.Instant;
import java.util.Set;

/** Domain event when a conversation is created. */
public record ConversationCreatedEvent(
    String tenantId,
    Long conversationId,
    ConversationType type,
    Set<Long> participantIds,
    Instant occurredAt) {

  public ConversationCreatedEvent(
      String tenantId, Long conversationId, ConversationType type, Set<Long> participantIds) {
    this(tenantId, conversationId, type, participantIds, Instant.now());
  }
}
