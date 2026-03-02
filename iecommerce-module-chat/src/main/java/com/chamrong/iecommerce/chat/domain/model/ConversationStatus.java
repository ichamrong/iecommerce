package com.chamrong.iecommerce.chat.domain.model;

/**
 * Lifecycle status of a conversation.
 */
public enum ConversationStatus {

  /** Active; participants can send messages. */
  OPEN,

  /** Closed; no new messages (e.g. support ticket resolved). */
  CLOSED,

  /** Archived for retention; read-only. */
  ARCHIVED
}
