package com.chamrong.iecommerce.common.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FilterHasherTest {

  @Test
  @DisplayName("same inputs produce same hash (stable ordering)")
  void stableOrdering() {
    String h1 =
        FilterHasher.computeHash("sale.listShifts", Map.of("tenantId", "t1", "status", "OPEN"));
    String h2 =
        FilterHasher.computeHash("sale.listShifts", Map.of("status", "OPEN", "tenantId", "t1"));
    assertThat(h1).isEqualTo(h2).hasSize(64).matches("^[0-9a-f]+$");
  }

  @Test
  @DisplayName("different endpoint produces different hash")
  void differentEndpoint() {
    String h1 = FilterHasher.computeHash("sale.listShifts", Map.of("tenantId", "t1"));
    String h2 = FilterHasher.computeHash("sale.listSessions", Map.of("tenantId", "t1"));
    assertThat(h1).isNotEqualTo(h2);
  }

  @Test
  @DisplayName("different filter values produce different hash")
  void differentValues() {
    String h1 = FilterHasher.computeHash("sale.listShifts", Map.of("tenantId", "t1"));
    String h2 = FilterHasher.computeHash("sale.listShifts", Map.of("tenantId", "t2"));
    assertThat(h1).isNotEqualTo(h2);
  }

  @Test
  @DisplayName("null values are excluded from hash")
  void nullValuesExcluded() {
    Map<String, Object> withNull = new HashMap<>();
    withNull.put("a", "1");
    withNull.put("b", null);
    String h1 = FilterHasher.computeHash("ep", withNull);
    String h2 = FilterHasher.computeHash("ep", Map.of("a", "1"));
    assertThat(h1).isEqualTo(h2);
  }

  @Test
  @DisplayName("empty filters with endpoint still produces hash")
  void emptyFiltersWithEndpoint() {
    String h = FilterHasher.computeHash("sale.listShifts", Map.of());
    assertThat(h).isNotBlank().hasSize(64);
  }

  @Test
  @DisplayName("null endpoint and null filters returns empty string")
  void nullEndpointAndFilters() {
    String h = FilterHasher.computeHash(null, null);
    assertThat(h).isEmpty();
  }

  @Test
  @DisplayName("empty endpoint and empty map returns empty string")
  void emptyEndpointAndEmptyMap() {
    String h = FilterHasher.computeHash("", Collections.emptyMap());
    assertThat(h).isEmpty();
  }
}
