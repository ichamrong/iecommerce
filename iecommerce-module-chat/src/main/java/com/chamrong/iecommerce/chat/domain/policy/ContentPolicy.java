package com.chamrong.iecommerce.chat.domain.policy;

/**
 * Content limits: message size, attachment count. Anti-abuse.
 */
public final class ContentPolicy {

  /** Max plain-text message length (bytes/characters). */
  public static final int MAX_MESSAGE_LENGTH = 4096;

  /** Max attachments per message. */
  public static final int MAX_ATTACHMENTS_PER_MESSAGE = 5;

  private ContentPolicy() {}

  public static void validateMessageLength(String content) {
    if (content != null && content.length() > MAX_MESSAGE_LENGTH) {
      throw new IllegalArgumentException(
          "Message exceeds max length of " + MAX_MESSAGE_LENGTH + " characters");
    }
  }

  public static void validateAttachmentCount(int count) {
    if (count > MAX_ATTACHMENTS_PER_MESSAGE) {
      throw new IllegalArgumentException(
          "Max " + MAX_ATTACHMENTS_PER_MESSAGE + " attachments per message");
    }
  }
}
