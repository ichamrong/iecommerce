package com.chamrong.iecommerce.chat.application;

import com.chamrong.iecommerce.chat.application.dto.ChatMessageResponse;
import com.chamrong.iecommerce.chat.application.dto.ConversationResponse;
import com.chamrong.iecommerce.chat.application.dto.SendMessageRequest;
import com.chamrong.iecommerce.chat.application.dto.StartConversationRequest;
import com.chamrong.iecommerce.chat.domain.ChatMessage;
import com.chamrong.iecommerce.chat.domain.Conversation;
import com.chamrong.iecommerce.chat.domain.policy.ContentPolicy;
import com.chamrong.iecommerce.chat.domain.ports.ConversationRepositoryPort;
import com.chamrong.iecommerce.chat.domain.ports.MessageRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ConversationRepositoryPort conversationRepository;
  private final MessageRepositoryPort messageRepository;

  // ── Conversations ──────────────────────────────────────────────────────────

  @Transactional
  public ConversationResponse startConversation(String tenantId, StartConversationRequest req) {
    Conversation conv = new Conversation();
    conv.setTenantId(tenantId);
    conv.setParticipantIds(new HashSet<>(req.participantIds()));
    return toConvResponse(conversationRepository.save(conv));
  }

  @Transactional(readOnly = true)
  public List<ConversationResponse> getMyConversations(String tenantId, Long userId) {
    return conversationRepository.findByTenantIdAndParticipantId(tenantId, userId).stream()
        .map(this::toConvResponse)
        .toList();
  }

  // ── Messages ───────────────────────────────────────────────────────────────

  @Transactional
  public ChatMessageResponse sendMessage(
      String tenantId, Long conversationId, SendMessageRequest req) {
    Conversation conv =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(
                () -> new EntityNotFoundException("Conversation not found: " + conversationId));
    com.chamrong.iecommerce.common.security.TenantGuard.requireSameTenant(
        conv.getTenantId(), tenantId);

    if (!conv.hasParticipant(req.senderId())) {
      throw new IllegalStateException("Sender is not a participant in this conversation");
    }
    ContentPolicy.validateMessageLength(req.content());

    ChatMessage msg = new ChatMessage();
    msg.setTenantId(conv.getTenantId());
    msg.setConversationId(conversationId);
    msg.setSenderId(req.senderId());
    msg.setContent(req.content());

    conv.updateLastMessage();
    conversationRepository.save(conv);

    return toMsgResponse(messageRepository.save(msg));
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private ConversationResponse toConvResponse(Conversation c) {
    return new ConversationResponse(
        c.getId(), c.getParticipantIds(), c.getLastMessageTimestamp(), c.getCreatedAt());
  }

  private ChatMessageResponse toMsgResponse(ChatMessage m) {
    return new ChatMessageResponse(
        m.getId(),
        m.getConversationId(),
        m.getSenderId(),
        m.getContent(),
        m.isRead(),
        m.getTimestamp());
  }
}
