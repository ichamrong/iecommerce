package com.chamrong.iecommerce.chat.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_conversation")
public class Conversation extends BaseTenantEntity {

  @ElementCollection
  @CollectionTable(
      name = "chat_conversation_participants",
      joinColumns = @JoinColumn(name = "conversation_id"))
  @Column(name = "participant_id")
  private Set<Long> participantIds = new HashSet<>();

  private Instant lastMessageTimestamp;

  // ── Domain behaviour ───────────────────────────────────────────────────────

  public void updateLastMessage() {
    this.lastMessageTimestamp = Instant.now();
  }

  public boolean hasParticipant(Long userId) {
    return participantIds.contains(userId);
  }
}
