package com.chamrong.iecommerce.chat.application;

import com.chamrong.iecommerce.chat.application.dto.ChatMessageResponse;
import com.chamrong.iecommerce.chat.application.dto.ConversationResponse;
import com.chamrong.iecommerce.chat.application.dto.SendMessageRequest;
import com.chamrong.iecommerce.chat.application.dto.StartConversationRequest;
import com.chamrong.iecommerce.chat.domain.ChatMessage;
import com.chamrong.iecommerce.chat.domain.ChatMessageRepository;
import com.chamrong.iecommerce.chat.domain.Conversation;
import com.chamrong.iecommerce.chat.domain.ConversationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ConversationRepository conversationRepository;
  private final ChatMessageRepository chatMessageRepository;

  // ── Conversations ──────────────────────────────────────────────────────────

  @Transactional
  public ConversationResponse startConversation(String tenantId, StartConversationRequest req) {
    Conversation conv = new Conversation();
    conv.setTenantId(tenantId);
    conv.setParticipantIds(new HashSet<>(req.participantIds()));
    return toConvResponse(conversationRepository.save(conv));
  }

  @Transactional(readOnly = true)
  public List<ConversationResponse> getMyConversations(Long userId) {
    return conversationRepository.findByParticipantIdsContaining(userId).stream()
        .map(this::toConvResponse)
        .toList();
  }

  // ── Messages ───────────────────────────────────────────────────────────────

  @Transactional
  public ChatMessageResponse sendMessage(Long conversationId, SendMessageRequest req) {
    Conversation conv =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(
                () -> new EntityNotFoundException("Conversation not found: " + conversationId));

    if (!conv.hasParticipant(req.senderId())) {
      throw new IllegalStateException("Sender is not a participant in this conversation");
    }

    ChatMessage msg = new ChatMessage();
    msg.setTenantId(conv.getTenantId());
    msg.setConversationId(conversationId);
    msg.setSenderId(req.senderId());
    msg.setContent(req.content());

    conv.updateLastMessage();
    conversationRepository.save(conv);

    return toMsgResponse(chatMessageRepository.save(msg));
  }

  @Transactional(readOnly = true)
  public List<ChatMessageResponse> getMessages(Long conversationId) {
    return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId).stream()
        .map(this::toMsgResponse)
        .toList();
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private ConversationResponse toConvResponse(Conversation c) {
    return new ConversationResponse(
        c.getId(), c.getParticipantIds(), c.getLastMessageTimestamp(), c.getCreatedAt());
  }

  private ChatMessageResponse toMsgResponse(ChatMessage m) {
    return new ChatMessageResponse(
        m.getId(), m.getConversationId(), m.getSenderId(), m.getContent(), m.isRead(), m.getTimestamp());
  }
}
