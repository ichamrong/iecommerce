package com.chamrong.iecommerce.chat.domain.model;

/**
 * Status of a message for display and audit.
 */
public enum MessageStatus {

  /** Delivered; visible. */
  SENT,

  /** Edited after send; show editedAt. */
  EDITED,

  /** Soft-deleted; tombstone for audit. */
  DELETED
}
