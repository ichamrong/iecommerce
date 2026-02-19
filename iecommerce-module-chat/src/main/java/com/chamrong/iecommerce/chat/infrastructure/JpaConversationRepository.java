package com.chamrong.iecommerce.chat.infrastructure;

import com.chamrong.iecommerce.chat.domain.Conversation;
import com.chamrong.iecommerce.chat.domain.ConversationRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaConversationRepository implements ConversationRepository {

  private final ConversationJpaInterface jpaInterface;

  public JpaConversationRepository(ConversationJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public Conversation save(Conversation conversation) {
    return jpaInterface.save(conversation);
  }

  @Override
  public Optional<Conversation> findById(Long id) {
    return jpaInterface.findById(id);
  }

  @Override
  public List<Conversation> findByParticipantIdsContaining(Long participantId) {
    return jpaInterface.findByParticipantIdsContaining(participantId);
  }

  public interface ConversationJpaInterface extends JpaRepository<Conversation, Long> {
    List<Conversation> findByParticipantIdsContaining(Long participantId);
  }
}
