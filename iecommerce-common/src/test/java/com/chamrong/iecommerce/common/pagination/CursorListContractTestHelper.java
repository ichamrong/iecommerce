package com.chamrong.iecommerce.common.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Reusable contract test helper for cursor-paginated list endpoints. Use in integration tests to
 * assert: response shape, limit respected, hasNext/nextCursor consistency, no duplicates across
 * pages, and optional ordering.
 */
public final class CursorListContractTestHelper {

  private CursorListContractTestHelper() {}

  /**
   * Asserts standard cursor page response shape: data not null, limit &gt; 0, hasNext implies
   * nextCursor non-blank and data size == limit.
   *
   * @param response cursor page response
   * @param limit expected limit
   */
  public static <T> void assertCursorPageShape(CursorPageResponse<T> response, int limit) {
    assertThat(response).isNotNull();
    assertThat(response.getData()).isNotNull();
    assertThat(response.getLimit()).isEqualTo(limit);
    if (response.isHasNext()) {
      assertThat(response.getNextCursor()).isNotBlank();
      assertThat(response.getData()).hasSize(limit);
    } else {
      assertThat(response.getData()).hasSizeLessThanOrEqualTo(limit);
    }
  }

  /**
   * Asserts that concatenating page1 data and page2 data contains no duplicate ids (by extracted
   * id).
   *
   * @param page1Data first page items
   * @param page2Data second page items
   * @param idExtractor extracts id from item (e.g. Entity::getId)
   */
  public static <T, I> void assertNoDuplicateIds(
      List<T> page1Data, List<T> page2Data, java.util.function.Function<T, I> idExtractor) {
    java.util.Set<I> ids1 =
        page1Data.stream().map(idExtractor).collect(java.util.stream.Collectors.toSet());
    for (T item : page2Data) {
      I id = idExtractor.apply(item);
      assertThat(ids1).doesNotContain(id);
    }
  }

  /**
   * Asserts that items are ordered by created_at descending. When created_at is equal, use a
   * separate assertion with idExtractor if needed (e.g. id DESC).
   */
  public static <T> void assertOrderingByCreatedAtDescending(
      List<T> data, java.util.function.Function<T, java.time.Instant> createdAtExtractor) {
    for (int i = 0; i < data.size() - 1; i++) {
      T curr = data.get(i);
      T next = data.get(i + 1);
      java.time.Instant currTs = createdAtExtractor.apply(curr);
      java.time.Instant nextTs = createdAtExtractor.apply(next);
      assertThat(currTs).as("created_at must be DESC at index %d", i).isAfterOrEqualTo(nextTs);
    }
  }
}
