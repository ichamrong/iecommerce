package com.chamrong.iecommerce.chat.application;

import com.chamrong.iecommerce.chat.domain.ChatMessage;
import com.chamrong.iecommerce.chat.domain.ChatMessageRepository;
import com.chamrong.iecommerce.chat.domain.Conversation;
import com.chamrong.iecommerce.chat.domain.ConversationRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

  private final ChatMessageRepository chatMessageRepository;
  private final ConversationRepository conversationRepository;

  public ChatService(
      ChatMessageRepository chatMessageRepository, ConversationRepository conversationRepository) {
    this.chatMessageRepository = chatMessageRepository;
    this.conversationRepository = conversationRepository;
  }

  @Transactional
  public Conversation startConversation(List<Long> participants) {
    Conversation conversation = new Conversation();
    conversation.getParticipantIds().addAll(participants);
    return conversationRepository.save(conversation);
  }

  @Transactional
  public ChatMessage sendMessage(Long conversationId, Long senderId, String content) {
    ChatMessage message = new ChatMessage();
    message.setConversationId(conversationId);
    message.setSenderId(senderId);
    message.setContent(content);
    message.setTimestamp(Instant.now());

    ChatMessage saved = chatMessageRepository.save(message);

    conversationRepository
        .findById(conversationId)
        .ifPresent(
            c -> {
              c.setLastMessageTimestamp(saved.getTimestamp());
              conversationRepository.save(c);
            });

    return saved;
  }

  public List<ChatMessage> getMessages(Long conversationId) {
    return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
  }
}
