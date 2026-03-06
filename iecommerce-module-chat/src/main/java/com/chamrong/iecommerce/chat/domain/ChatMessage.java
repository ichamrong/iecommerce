package com.chamrong.iecommerce.chat.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "chat_message")
public class ChatMessage extends BaseTenantEntity {

  @Column(nullable = false)
  private Long conversationId;

  @Column(nullable = false)
  private Long senderId;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(nullable = false)
  private Instant timestamp = Instant.now();

  @Column(name = "is_read", nullable = false)
  private boolean read = false;

  public ChatMessage() {}

  public ChatMessage(String tenantId, Long conversationId, Long senderId, String content) {
    setTenantId(tenantId);
    this.conversationId = conversationId;
    this.senderId = senderId;
    this.content = content;
    this.timestamp = Instant.now();
  }

  public Long getConversationId() {
    return conversationId;
  }

  public void setConversationId(Long conversationId) {
    this.conversationId = conversationId;
  }

  public Long getSenderId() {
    return senderId;
  }

  public void setSenderId(Long senderId) {
    this.senderId = senderId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public void markRead() {
    this.read = true;
  }
}
