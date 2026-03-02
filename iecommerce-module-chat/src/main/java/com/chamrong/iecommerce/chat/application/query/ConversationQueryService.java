package com.chamrong.iecommerce.chat.application.query;

import com.chamrong.iecommerce.chat.application.dto.ConversationResponse;
import com.chamrong.iecommerce.chat.domain.Conversation;
import com.chamrong.iecommerce.chat.domain.ports.ConversationRepositoryPort;
import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.common.security.TenantGuard;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationQueryService {

  public static final String ENDPOINT_LIST_CONVERSATIONS = "chat:listConversations";

  private static final int DEFAULT_LIMIT = 20;
  private static final int MAX_LIMIT = 100;

  private final ConversationRepositoryPort conversationRepository;

  @Transactional(readOnly = true)
  public Optional<ConversationResponse> findById(String tenantId, Long id, Long actorId) {
    Optional<Conversation> opt = conversationRepository.findById(id);
    if (opt.isEmpty()) return Optional.empty();
    Conversation c = opt.get();
    TenantGuard.requireSameTenant(c.getTenantId(), tenantId);
    if (!c.hasParticipant(actorId)) {
      return Optional.empty();
    }
    return Optional.of(toResponse(c));
  }

  /**
   * Cursor-paginated list of conversations for the participant. filterHash binds cursor to
   * participantId.
   */
  @Transactional(readOnly = true)
  public CursorPageResponse<ConversationResponse> findPage(
      String tenantId, Long participantId, String cursor, int limit) {
    int effectiveLimit = Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, MAX_LIMIT);
    int fetchSize = effectiveLimit + 1;
    Map<String, Object> filterMap = new LinkedHashMap<>();
    filterMap.put("participantId", participantId);
    String filterHash = FilterHasher.computeHash(ENDPOINT_LIST_CONVERSATIONS, filterMap);

    Instant afterCreatedAt = null;
    Long afterId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      afterCreatedAt = payload.getCreatedAt();
      try {
        afterId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new InvalidCursorException(InvalidCursorException.INVALID_CURSOR, "Invalid cursor id");
      }
    }

    List<Conversation> rows =
        conversationRepository.findCursorPage(
            tenantId, participantId, afterCreatedAt, afterId, fetchSize);
    boolean hasNext = rows.size() == fetchSize;
    List<Conversation> page = hasNext ? rows.subList(0, effectiveLimit) : rows;

    String nextCursor = null;
    if (hasNext && !page.isEmpty()) {
      Conversation last = page.get(page.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), String.valueOf(last.getId()), filterHash));
    }

    List<ConversationResponse> data = page.stream().map(this::toResponse).toList();
    return CursorPageResponse.of(data, nextCursor, hasNext, effectiveLimit);
  }

  private ConversationResponse toResponse(Conversation c) {
    return new ConversationResponse(
        c.getId(),
        c.getParticipantIds(),
        c.getLastMessageTimestamp(),
        c.getCreatedAt());
  }
}
