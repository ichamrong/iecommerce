package com.chamrong.iecommerce.chat.domain.ports;

import com.chamrong.iecommerce.chat.domain.ChatMessage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Port for message persistence. Implementations in infrastructure.
 */
public interface MessageRepositoryPort {

  ChatMessage save(ChatMessage message);

  Optional<ChatMessage> findById(Long id);

  /**
   * Keyset paginated messages in a conversation. Order: created_at DESC, id DESC.
   *
   * @param afterCreatedAt cursor; null → first page
   * @param afterId cursor tie-break; null → first page
   * @param limit max rows
   */
  List<ChatMessage> findCursorPage(
      String tenantId,
      Long conversationId,
      Instant afterCreatedAt,
      Long afterId,
      int limit);
}
