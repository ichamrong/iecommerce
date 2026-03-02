package com.chamrong.iecommerce.catalog.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchNormalizationServiceTest {

  @Test
  @DisplayName("normalizeKeyword returns null for null or blank")
  void normalizeKeyword_nullOrBlank_returnsNull() {
    assertThat(SearchNormalizationService.normalizeKeyword(null)).isNull();
    assertThat(SearchNormalizationService.normalizeKeyword("")).isNull();
    assertThat(SearchNormalizationService.normalizeKeyword("   ")).isNull();
  }

  @Test
  @DisplayName("normalizeKeyword trims and lowercases")
  void normalizeKeyword_trimAndLowercase() {
    assertThat(SearchNormalizationService.normalizeKeyword("  Foo Bar  ")).isEqualTo("foo bar");
    assertThat(SearchNormalizationService.normalizeKeyword("UPPER")).isEqualTo("upper");
  }
}
