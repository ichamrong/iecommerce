package com.chamrong.iecommerce.chat.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.chat.domain.ChatMessage;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMessageRepository extends JpaRepository<ChatMessage, Long> {

  List<ChatMessage> findByConversationIdOrderByTimestampAsc(Long conversationId);

  @Query(
      """
      SELECT m FROM ChatMessage m
      WHERE m.tenantId = :tenantId AND m.conversationId = :conversationId
      ORDER BY m.createdAt DESC, m.id DESC
      """)
  List<ChatMessage> findFirstPage(
      @Param("tenantId") String tenantId,
      @Param("conversationId") Long conversationId,
      org.springframework.data.domain.Pageable pageable);

  @Query(
      """
      SELECT m FROM ChatMessage m
      WHERE m.tenantId = :tenantId AND m.conversationId = :conversationId
        AND (m.createdAt < :afterCreatedAt OR (m.createdAt = :afterCreatedAt AND m.id < :afterId))
      ORDER BY m.createdAt DESC, m.id DESC
      """)
  List<ChatMessage> findNextPage(
      @Param("tenantId") String tenantId,
      @Param("conversationId") Long conversationId,
      @Param("afterCreatedAt") Instant afterCreatedAt,
      @Param("afterId") Long afterId,
      org.springframework.data.domain.Pageable pageable);
}
