package com.chamrong.iecommerce.chat.infrastructure.persistence;

import com.chamrong.iecommerce.chat.domain.Conversation;
import com.chamrong.iecommerce.chat.domain.ports.ConversationRepositoryPort;
import com.chamrong.iecommerce.chat.infrastructure.persistence.jpa.SpringDataConversationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaConversationRepositoryAdapter implements ConversationRepositoryPort {

  private final SpringDataConversationRepository jpaRepo;

  @Override
  public Conversation save(Conversation conversation) {
    return jpaRepo.save(conversation);
  }

  @Override
  public Optional<Conversation> findById(Long id) {
    return jpaRepo.findById(id);
  }

  @Override
  public List<Conversation> findByTenantIdAndParticipantId(String tenantId, Long participantId) {
    return jpaRepo.findByTenantIdAndParticipantId(tenantId, participantId);
  }

  @Override
  public List<Conversation> findCursorPage(
      String tenantId,
      Long participantId,
      Instant afterCreatedAt,
      Long afterId,
      int limit) {
    if (afterCreatedAt == null || afterId == null) {
      return jpaRepo.findFirstPage(tenantId, participantId, PageRequest.of(0, limit));
    }
    return jpaRepo.findNextPage(
        tenantId, participantId, afterCreatedAt, afterId, PageRequest.of(0, limit));
  }
}
