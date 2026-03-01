package com.chamrong.iecommerce.sale.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.sale.application.dto.ShiftResponse;
import com.chamrong.iecommerce.sale.domain.model.Shift;
import com.chamrong.iecommerce.sale.domain.ports.ShiftRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cursor pagination contract tests for Sale list endpoints. Asserts: first page shape, filterHash
 * binding, and INVALID_CURSOR_FILTER_MISMATCH when cursor from different filters is used.
 */
@ExtendWith(MockitoExtension.class)
class SaleQueryServiceCursorTest {

  private static final String TENANT_ID = "tenant-1";
  private static final String ENDPOINT = "sale:listShifts";

  @Mock private ShiftRepositoryPort shiftRepository;

  private SaleQueryService queryService;

  @BeforeEach
  void setUp() {
    queryService = new SaleQueryService(null, null, shiftRepository, null);
  }

  @Test
  void listShifts_firstPage_returnsCursorPageResponseWithCorrectShape() {
    Shift shift = shift(1L, Instant.now());
    when(shiftRepository.findPage(eq(TENANT_ID), eq((Instant) null), eq((Long) null), eq(21)))
        .thenReturn(List.of(shift));

    CursorPageResponse<ShiftResponse> response =
        queryService.listShifts(TENANT_ID, null, 20, Map.of());

    assertThat(response).isNotNull();
    assertThat(response.getData()).hasSize(1);
    assertThat(response.getLimit()).isEqualTo(20);
    assertThat(response.getData().getFirst().id()).isEqualTo(1L);
    assertThat(response.isHasNext()).isFalse();
    assertThat(response.getNextCursor()).isNull();
  }

  @Test
  void listShifts_withCursorAndMatchingFilterHash_returnsNextPage() {
    Instant t = Instant.now();
    Shift s2 = shift(1L, t.minusSeconds(1));
    String filterHash = FilterHasher.computeHash(ENDPOINT, Map.of());
    String cursor = CursorCodec.encode(new CursorPayload(1, t, "2", filterHash));

    when(shiftRepository.findPage(eq(TENANT_ID), eq(t), eq(2L), eq(21))).thenReturn(List.of(s2));

    CursorPageResponse<ShiftResponse> response =
        queryService.listShifts(TENANT_ID, cursor, 20, Map.of());

    assertThat(response).isNotNull();
    assertThat(response.getData()).hasSize(1);
    assertThat(response.getData().getFirst().id()).isEqualTo(1L);
  }

  @Test
  void listShifts_withCursorAndDifferentFilterHash_throwsInvalidCursorFilterMismatch() {
    String filterHashA = FilterHasher.computeHash(ENDPOINT, Map.of("status", "OPEN"));
    String cursor = CursorCodec.encode(new CursorPayload(1, Instant.now(), "1", filterHashA));

    assertThatThrownBy(
            () -> queryService.listShifts(TENANT_ID, cursor, 20, Map.of("status", "CLOSED")))
        .isInstanceOf(InvalidCursorException.class)
        .satisfies(
            ex ->
                assertThat(((InvalidCursorException) ex).getErrorCode())
                    .isEqualTo(InvalidCursorException.INVALID_CURSOR_FILTER_MISMATCH));
  }

  private static Shift shift(long id, Instant createdAt) {
    return new Shift(
        id, TENANT_ID, 0L, "staff", "term", createdAt, null, Shift.ShiftStatus.OPEN, createdAt);
  }
}
