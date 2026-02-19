package com.chamrong.iecommerce.chat.domain;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository {
  Conversation save(Conversation conversation);

  Optional<Conversation> findById(Long id);

  List<Conversation> findByParticipantIdsContaining(Long participantId);
}
