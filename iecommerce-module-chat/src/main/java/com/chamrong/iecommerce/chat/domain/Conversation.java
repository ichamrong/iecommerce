package com.chamrong.iecommerce.chat.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_conversation")
public class Conversation extends BaseTenantEntity {

  @ElementCollection
  @CollectionTable(
      name = "chat_conversation_participants",
      joinColumns = @JoinColumn(name = "conversation_id"))
  @Column(name = "participant_id")
  private Set<Long> participantIds = new HashSet<>();

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  private Instant lastMessageTimestamp;

  public Set<Long> getParticipantIds() {
    return participantIds;
  }

  public void setParticipantIds(Set<Long> participantIds) {
    this.participantIds = participantIds;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getLastMessageTimestamp() {
    return lastMessageTimestamp;
  }

  public void setLastMessageTimestamp(Instant lastMessageTimestamp) {
    this.lastMessageTimestamp = lastMessageTimestamp;
  }
}
