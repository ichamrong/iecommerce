package com.chamrong.iecommerce.chat.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

  @Column(nullable = false)
  private boolean read = false;
}
