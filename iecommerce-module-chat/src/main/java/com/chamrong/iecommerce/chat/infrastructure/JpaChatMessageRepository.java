package com.chamrong.iecommerce.chat.infrastructure;

import com.chamrong.iecommerce.chat.domain.ChatMessage;
import com.chamrong.iecommerce.chat.domain.ChatMessageRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaChatMessageRepository implements ChatMessageRepository {

  private final ChatMessageJpaInterface jpaInterface;

  public JpaChatMessageRepository(ChatMessageJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public ChatMessage save(ChatMessage message) {
    return jpaInterface.save(message);
  }

  @Override
  public List<ChatMessage> findByConversationIdOrderByTimestampAsc(Long conversationId) {
    return jpaInterface.findByConversationIdOrderByTimestampAsc(conversationId);
  }

  public interface ChatMessageJpaInterface extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversationIdOrderByTimestampAsc(Long conversationId);
  }
}
