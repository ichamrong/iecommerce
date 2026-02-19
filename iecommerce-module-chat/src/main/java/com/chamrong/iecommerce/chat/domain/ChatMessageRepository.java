package com.chamrong.iecommerce.chat.domain;

import java.util.List;

public interface ChatMessageRepository {
  ChatMessage save(ChatMessage message);

  List<ChatMessage> findByConversationIdOrderByTimestampAsc(Long conversationId);
}
