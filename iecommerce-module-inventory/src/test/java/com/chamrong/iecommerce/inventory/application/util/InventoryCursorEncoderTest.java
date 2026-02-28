package com.chamrong.iecommerce.inventory.application.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InventoryCursorEncoder}. */
class InventoryCursorEncoderTest {

  @Test
  void encode_roundTrip_returnsOriginalValues() {
    Instant ts = Instant.parse("2025-03-01T10:00:00Z");
    Long id = 42L;

    String token = InventoryCursorEncoder.encode(ts, id);
    var decoded = InventoryCursorEncoder.decode(token);

    assertThat(decoded).isNotNull();
    assertThat(decoded.createdAt().getEpochSecond()).isEqualTo(ts.getEpochSecond());
    assertThat(decoded.id()).isEqualTo(id);
  }

  @Test
  void encode_isDeterministic() {
    Instant ts = Instant.parse("2025-01-15T00:00:00Z");
    String token1 = InventoryCursorEncoder.encode(ts, 99L);
    String token2 = InventoryCursorEncoder.encode(ts, 99L);
    assertThat(token1).isEqualTo(token2);
  }

  @Test
  void decode_null_returnsNull() {
    assertThat(InventoryCursorEncoder.decode(null)).isNull();
  }

  @Test
  void decode_blank_returnsNull() {
    assertThat(InventoryCursorEncoder.decode("   ")).isNull();
  }

  @Test
  void decode_invalidBase64_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> InventoryCursorEncoder.decode("!!!not-base64!!!"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void decode_validBase64ButMissingId_throwsMalformed() {
    String bad =
        java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("1234567890".getBytes());
    assertThatThrownBy(() -> InventoryCursorEncoder.decode(bad))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Malformed");
  }

  @Test
  void encode_decode_preservesLargeId() {
    Instant ts = Instant.now();
    Long largeId = Long.MAX_VALUE;
    var decoded = InventoryCursorEncoder.decode(InventoryCursorEncoder.encode(ts, largeId));
    assertThat(decoded.id()).isEqualTo(largeId);
  }
}
