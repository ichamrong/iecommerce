package com.chamrong.iecommerce.audit.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

import com.chamrong.iecommerce.audit.application.dto.AuditSearchFilters;
import com.chamrong.iecommerce.audit.domain.ports.AuditEventRepositoryPort;
import com.chamrong.iecommerce.audit.domain.ports.AuditTamperProofPort;
import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cursor contract: filterHash mismatch returns INVALID_CURSOR_FILTER_MISMATCH (400). Repository
 * must not be called with wrong cursor.
 */
@ExtendWith(MockitoExtension.class)
class AuditQueryServiceCursorFilterMismatchTest {

  private static final String TENANT = "tenant-1";

  @Mock private AuditEventRepositoryPort repository;
  @Mock private AuditTamperProofPort tamperProof;

  @InjectMocks private AuditQueryService queryService;

  @Test
  void findPage_whenCursorHasDifferentFilterHash_throwsInvalidCursorFilterMismatch() {
    String filterHashA =
        FilterHasher.computeHash(
            AuditQueryService.ENDPOINT_LIST_EVENTS, Map.of("actorId", "user1"));
    String filterHashB =
        FilterHasher.computeHash(
            AuditQueryService.ENDPOINT_LIST_EVENTS, Map.of("actorId", "user2"));
    String cursorFromFilterA =
        CursorCodec.encode(
            new CursorPayload(1, Instant.parse("2025-03-01T12:00:00Z"), "99", filterHashA));

    AuditSearchFilters filtersB =
        new AuditSearchFilters("user2", null, null, null, null, null, null, null, null);
    Map<String, Object> filterMapB = AuditQueryService.toFilterMap(filtersB);

    assertThatThrownBy(
            () -> queryService.findPage(TENANT, filtersB, cursorFromFilterA, 20, filterMapB))
        .isInstanceOf(InvalidCursorException.class)
        .satisfies(
            e ->
                assertThat(((InvalidCursorException) e).getErrorCode())
                    .isEqualTo(InvalidCursorException.INVALID_CURSOR_FILTER_MISMATCH));

    verifyNoInteractions(repository);
  }
}
