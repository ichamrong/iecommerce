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

  public Conversation() {}

  public Conversation(String tenantId, Set<Long> participantIds) {
    setTenantId(tenantId);
    this.participantIds.addAll(participantIds);
  }

  public Set<Long> getParticipantIds() {
    return participantIds;
  }

  public void setParticipantIds(Set<Long> participantIds) {
    this.participantIds = participantIds;
  }

  public Instant getLastMessageTimestamp() {
    return lastMessageTimestamp;
  }

  public void setLastMessageTimestamp(Instant lastMessageTimestamp) {
    this.lastMessageTimestamp = lastMessageTimestamp;
  }

  // ── Domain behaviour ───────────────────────────────────────────────────────

  public void updateLastMessage() {
    this.lastMessageTimestamp = Instant.now();
  }

  public boolean hasParticipant(Long userId) {
    return participantIds.contains(userId);
  }
}
