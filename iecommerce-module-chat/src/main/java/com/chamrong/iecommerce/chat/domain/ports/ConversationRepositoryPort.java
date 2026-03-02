package com.chamrong.iecommerce.chat.domain.ports;

import com.chamrong.iecommerce.chat.domain.Conversation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Port for conversation persistence. Implementations in infrastructure.
 */
public interface ConversationRepositoryPort {

  Conversation save(Conversation conversation);

  Optional<Conversation> findById(Long id);

  /** Conversations where the participant is a member. Tenant-scoped. */
  List<Conversation> findByTenantIdAndParticipantId(String tenantId, Long participantId);

  /**
   * Keyset paginated list for a participant. Order: created_at DESC, id DESC.
   *
   * @param afterCreatedAt cursor; null → first page
   * @param afterId cursor tie-break; null → first page
   * @param limit max rows
   */
  List<Conversation> findCursorPage(
      String tenantId,
      Long participantId,
      Instant afterCreatedAt,
      Long afterId,
      int limit);
}
