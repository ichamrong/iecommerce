package com.chamrong.iecommerce.chat.infrastructure.persistence;

import com.chamrong.iecommerce.chat.domain.ChatMessage;
import com.chamrong.iecommerce.chat.domain.ports.MessageRepositoryPort;
import com.chamrong.iecommerce.chat.infrastructure.persistence.jpa.SpringDataMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaMessageRepositoryAdapter implements MessageRepositoryPort {

  private final SpringDataMessageRepository jpaRepo;

  @Override
  public ChatMessage save(ChatMessage message) {
    return jpaRepo.save(message);
  }

  @Override
  public Optional<ChatMessage> findById(Long id) {
    return jpaRepo.findById(id);
  }

  @Override
  public List<ChatMessage> findCursorPage(
      String tenantId, Long conversationId, Instant afterCreatedAt, Long afterId, int limit) {
    if (afterCreatedAt == null || afterId == null) {
      return jpaRepo.findFirstPage(tenantId, conversationId, PageRequest.of(0, limit));
    }
    return jpaRepo.findNextPage(
        tenantId, conversationId, afterCreatedAt, afterId, PageRequest.of(0, limit));
  }
}
