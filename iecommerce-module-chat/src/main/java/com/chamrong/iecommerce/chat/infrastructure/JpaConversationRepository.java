package com.chamrong.iecommerce.chat.infrastructure;

import com.chamrong.iecommerce.chat.domain.Conversation;
import com.chamrong.iecommerce.chat.domain.ConversationRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link ConversationRepository} port. */
@Repository
public interface JpaConversationRepository
    extends JpaRepository<Conversation, Long>, ConversationRepository {
  @Override
  List<Conversation> findByParticipantIdsContaining(Long participantId);
}
