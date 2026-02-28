package com.chamrong.iecommerce.catalog.application.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chamrong.iecommerce.catalog.application.util.CatalogCursorEncoder.CatalogCursorDecoded;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CatalogCursorEncoder}. */
class CatalogCursorEncoderTest {

  @Test
  void encodeAndDecode_roundTrip() {
    Instant ts = Instant.ofEpochSecond(1_700_000_000L);
    long id = 42L;

    String token = CatalogCursorEncoder.encode(ts, id);
    assertThat(token).isNotBlank().doesNotContain("="); // no padding

    CatalogCursorDecoded decoded = CatalogCursorEncoder.decode(token);
    assertThat(decoded).isNotNull();
    assertThat(decoded.createdAt()).isEqualTo(ts);
    assertThat(decoded.id()).isEqualTo(id);
  }

  @Test
  void decode_null_returnsNull() {
    assertThat(CatalogCursorEncoder.decode(null)).isNull();
    assertThat(CatalogCursorEncoder.decode("")).isNull();
    assertThat(CatalogCursorEncoder.decode("  ")).isNull();
  }

  @Test
  void decode_malformedToken_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> CatalogCursorEncoder.decode("notbase64!!!!!"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void decode_validBase64ButWrongFormat_throwsIllegalArgumentException() {
    // Base64 of a string without comma separator
    String bad =
        java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("1234567890".getBytes());
    assertThatThrownBy(() -> CatalogCursorEncoder.decode(bad))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Malformed");
  }

  @Test
  void token_isDeterministic() {
    Instant ts = Instant.ofEpochSecond(1_000_000L);
    String t1 = CatalogCursorEncoder.encode(ts, 99L);
    String t2 = CatalogCursorEncoder.encode(ts, 99L);
    assertThat(t1).isEqualTo(t2);
  }
}
