package com.chamrong.iecommerce.chat.infrastructure;

import com.chamrong.iecommerce.chat.domain.ChatMessage;
import com.chamrong.iecommerce.chat.domain.ChatMessageRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link ChatMessageRepository} port. */
@Repository
public interface JpaChatMessageRepository
    extends JpaRepository<ChatMessage, Long>, ChatMessageRepository {
  @Override
  List<ChatMessage> findByConversationIdOrderByTimestampAsc(Long conversationId);
}
