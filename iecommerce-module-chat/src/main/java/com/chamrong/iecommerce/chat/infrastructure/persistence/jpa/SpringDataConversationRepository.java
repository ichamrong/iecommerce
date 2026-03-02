package com.chamrong.iecommerce.chat.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.chat.domain.Conversation;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataConversationRepository extends JpaRepository<Conversation, Long> {

  @Query(
      """
      SELECT c FROM Conversation c
      JOIN c.participantIds p
      WHERE c.tenantId = :tenantId AND p = :participantId
      ORDER BY c.createdAt DESC, c.id DESC
      """)
  List<Conversation> findByTenantIdAndParticipantId(
      @Param("tenantId") String tenantId, @Param("participantId") Long participantId);

  @Query(
      """
      SELECT c FROM Conversation c
      JOIN c.participantIds p
      WHERE c.tenantId = :tenantId AND p = :participantId
      ORDER BY c.createdAt DESC, c.id DESC
      """)
  List<Conversation> findFirstPage(
      @Param("tenantId") String tenantId,
      @Param("participantId") Long participantId,
      org.springframework.data.domain.Pageable pageable);

  @Query(
      """
      SELECT c FROM Conversation c
      JOIN c.participantIds p
      WHERE c.tenantId = :tenantId AND p = :participantId
        AND (c.createdAt < :afterCreatedAt OR (c.createdAt = :afterCreatedAt AND c.id < :afterId))
      ORDER BY c.createdAt DESC, c.id DESC
      """)
  List<Conversation> findNextPage(
      @Param("tenantId") String tenantId,
      @Param("participantId") Long participantId,
      @Param("afterCreatedAt") Instant afterCreatedAt,
      @Param("afterId") Long afterId,
      org.springframework.data.domain.Pageable pageable);
}
