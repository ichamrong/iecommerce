package com.chamrong.iecommerce.chat.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.chat.domain.ports.ConversationRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConversationQueryServiceCursorTest {

  private static final String TENANT = "tenant-1";

  @Mock private ConversationRepositoryPort conversationRepository;

  private ConversationQueryService service;

  @BeforeEach
  void setUp() {
    service = new ConversationQueryService(conversationRepository);
    TenantContext.setCurrentTenant(TENANT);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void findPage_firstPage_returnsCursorPageResponse() {
    when(conversationRepository.findCursorPage(
            eq(TENANT), eq(1L), eq(null), eq(null), any(Integer.class)))
        .thenReturn(List.of());

    var result = service.findPage(TENANT, 1L, null, 20);

    assertThat(result.getData()).isEmpty();
    assertThat(result.getNextCursor()).isNull();
    assertThat(result.isHasNext()).isFalse();
    assertThat(result.getLimit()).isEqualTo(20);
  }

  @Test
  void findPage_cursorFromDifferentParticipant_throwsFilterMismatch() {
    String hashParticipant1 =
        FilterHasher.computeHash(
            ConversationQueryService.ENDPOINT_LIST_CONVERSATIONS,
            java.util.Map.of("participantId", 1L));
    String cursor = CursorCodec.encode(new CursorPayload(1, Instant.now(), "1", hashParticipant1));

    assertThatThrownBy(() -> service.findPage(TENANT, 2L, cursor, 20))
        .isInstanceOf(InvalidCursorException.class)
        .satisfies(
            e ->
                assertThat(((InvalidCursorException) e).getErrorCode())
                    .isEqualTo(InvalidCursorException.INVALID_CURSOR_FILTER_MISMATCH));
  }
}
