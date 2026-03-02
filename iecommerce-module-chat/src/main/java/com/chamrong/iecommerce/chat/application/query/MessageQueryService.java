package com.chamrong.iecommerce.chat.application.query;

import com.chamrong.iecommerce.chat.application.dto.ChatMessageResponse;
import com.chamrong.iecommerce.chat.domain.ChatMessage;
import com.chamrong.iecommerce.chat.domain.Conversation;
import com.chamrong.iecommerce.chat.domain.ports.ConversationRepositoryPort;
import com.chamrong.iecommerce.chat.domain.ports.MessageRepositoryPort;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageQueryService {

  public static final String ENDPOINT_LIST_MESSAGES = "chat:listMessages";

  private static final int DEFAULT_LIMIT = 50;
  private static final int MAX_LIMIT = 100;

  private final MessageRepositoryPort messageRepository;
  private final ConversationRepositoryPort conversationRepository;

  /**
   * Cursor-paginated messages in a conversation. Caller must be participant (enforced by
   * controller). filterHash includes conversationId.
   */
  @Transactional(readOnly = true)
  public CursorPageResponse<ChatMessageResponse> findPage(
      String tenantId, Long conversationId, Long actorId, String cursor, int limit) {
    Conversation conv =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(
                () -> new jakarta.persistence.EntityNotFoundException("Conversation not found"));
    TenantGuard.requireSameTenant(conv.getTenantId(), tenantId);
    if (!conv.hasParticipant(actorId)) {
      throw new com.chamrong.iecommerce.chat.domain.exception.ChatDomainException(
          "Not a participant of this conversation");
    }

    int effectiveLimit = Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, MAX_LIMIT);
    int fetchSize = effectiveLimit + 1;
    Map<String, Object> filterMap = new LinkedHashMap<>();
    filterMap.put("conversationId", conversationId);
    String filterHash = FilterHasher.computeHash(ENDPOINT_LIST_MESSAGES, filterMap);

    Instant afterCreatedAt = null;
    Long afterId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      afterCreatedAt = payload.getCreatedAt();
      try {
        afterId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new InvalidCursorException(
            InvalidCursorException.INVALID_CURSOR, "Invalid cursor id");
      }
    }

    List<ChatMessage> rows =
        messageRepository.findCursorPage(
            tenantId, conversationId, afterCreatedAt, afterId, fetchSize);
    boolean hasNext = rows.size() == fetchSize;
    List<ChatMessage> page = hasNext ? rows.subList(0, effectiveLimit) : rows;

    String nextCursor = null;
    if (hasNext && !page.isEmpty()) {
      ChatMessage last = page.get(page.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), String.valueOf(last.getId()), filterHash));
    }

    List<ChatMessageResponse> data = page.stream().map(this::toResponse).toList();
    return CursorPageResponse.of(data, nextCursor, hasNext, effectiveLimit);
  }

  private ChatMessageResponse toResponse(ChatMessage m) {
    return new ChatMessageResponse(
        m.getId(),
        m.getConversationId(),
        m.getSenderId(),
        m.getContent(),
        m.isRead(),
        m.getTimestamp());
  }
}
