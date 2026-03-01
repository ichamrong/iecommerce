package com.chamrong.iecommerce.common.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CursorPageResponseTest {

  @Test
  void lastPage_hasNoNextCursor() {
    CursorPageResponse<String> page = CursorPageResponse.lastPage(List.of("a", "b"), 10);
    assertThat(page.getData()).containsExactly("a", "b");
    assertThat(page.getNextCursor()).isNull();
    assertThat(page.isHasNext()).isFalse();
    assertThat(page.getLimit()).isEqualTo(10);
  }

  @Test
  void withNext_hasNextCursor() {
    CursorPageResponse<String> page = CursorPageResponse.withNext(List.of("a"), "cursor123", 10);
    assertThat(page.getData()).containsExactly("a");
    assertThat(page.getNextCursor()).isEqualTo("cursor123");
    assertThat(page.isHasNext()).isTrue();
    assertThat(page.getLimit()).isEqualTo(10);
  }

  @Test
  void of_nullData_becomesEmptyList() {
    CursorPageResponse<String> page = CursorPageResponse.of(null, "c", true, 5);
    assertThat(page.getData()).isEmpty();
  }
}
