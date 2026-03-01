package com.chamrong.iecommerce.common.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CursorCodecTest {

  @Nested
  @DisplayName("encode then decode roundtrip")
  class Roundtrip {

    @Test
    void roundtrip_with_all_fields() {
      CursorPayload payload =
          new CursorPayload(1, Instant.parse("2025-03-01T12:00:00Z"), "12345", "abc123hash");
      String encoded = CursorCodec.encode(payload);
      assertThat(encoded).isNotBlank().doesNotContain("+").doesNotContain("/").doesNotContain("=");

      CursorPayload decoded = CursorCodec.decode(encoded);
      assertThat(decoded.getV()).isEqualTo(1);
      assertThat(decoded.getCreatedAt()).isEqualTo(Instant.parse("2025-03-01T12:00:00Z"));
      assertThat(decoded.getId()).isEqualTo("12345");
      assertThat(decoded.getFilterHash()).isEqualTo("abc123hash");
    }

    @Test
    void roundtrip_empty_filterHash() {
      CursorPayload payload = new CursorPayload(1, Instant.EPOCH, "0", "");
      String encoded = CursorCodec.encode(payload);
      CursorPayload decoded = CursorCodec.decode(encoded);
      assertThat(decoded.getFilterHash()).isEmpty();
    }

    @Test
    void roundtrip_special_chars_in_id() {
      CursorPayload payload = new CursorPayload(1, Instant.EPOCH, "id-with-dash", "h");
      String encoded = CursorCodec.encode(payload);
      CursorPayload decoded = CursorCodec.decode(encoded);
      assertThat(decoded.getId()).isEqualTo("id-with-dash");
    }
  }

  @Nested
  @DisplayName("invalid cursor decode returns InvalidCursorException")
  class InvalidCursor {

    @Test
    void null_cursor() {
      assertThatThrownBy(() -> CursorCodec.decode(null))
          .isInstanceOf(InvalidCursorException.class)
          .hasMessageContaining("null or blank")
          .satisfies(
              e ->
                  assertThat(((InvalidCursorException) e).getErrorCode())
                      .isEqualTo(InvalidCursorException.INVALID_CURSOR));
    }

    @Test
    void blank_cursor() {
      assertThatThrownBy(() -> CursorCodec.decode("   "))
          .isInstanceOf(InvalidCursorException.class)
          .satisfies(
              e ->
                  assertThat(((InvalidCursorException) e).getErrorCode())
                      .isEqualTo(InvalidCursorException.INVALID_CURSOR));
    }

    @Test
    void invalid_base64() {
      assertThatThrownBy(() -> CursorCodec.decode("not-valid-base64!!"))
          .isInstanceOf(InvalidCursorException.class)
          .satisfies(
              e ->
                  assertThat(((InvalidCursorException) e).getErrorCode())
                      .isEqualTo(InvalidCursorException.INVALID_CURSOR));
    }

    @Test
    void unsupported_version() {
      CursorPayload payload = new CursorPayload(99, Instant.EPOCH, "1", "");
      String encoded = CursorCodec.encode(payload);
      assertThatThrownBy(() -> CursorCodec.decode(encoded))
          .isInstanceOf(InvalidCursorException.class)
          .satisfies(
              e ->
                  assertThat(((InvalidCursorException) e).getErrorCode())
                      .isEqualTo(InvalidCursorException.INVALID_CURSOR_VERSION));
    }
  }

  @Nested
  @DisplayName("decodeAndValidateFilter")
  class FilterValidation {

    @Test
    void matching_hash_passes() {
      CursorPayload payload = new CursorPayload(1, Instant.EPOCH, "1", "sameHash");
      String encoded = CursorCodec.encode(payload);
      CursorPayload decoded = CursorCodec.decodeAndValidateFilter(encoded, "sameHash");
      assertThat(decoded.getId()).isEqualTo("1");
    }

    @Test
    void mismatch_throws() {
      CursorPayload payload = new CursorPayload(1, Instant.EPOCH, "1", "hashA");
      String encoded = CursorCodec.encode(payload);
      assertThatThrownBy(() -> CursorCodec.decodeAndValidateFilter(encoded, "hashB"))
          .isInstanceOf(InvalidCursorException.class)
          .satisfies(
              e ->
                  assertThat(((InvalidCursorException) e).getErrorCode())
                      .isEqualTo(InvalidCursorException.INVALID_CURSOR_FILTER_MISMATCH));
    }

    @Test
    void null_expected_hash_skips_check() {
      CursorPayload payload = new CursorPayload(1, Instant.EPOCH, "1", "any");
      String encoded = CursorCodec.encode(payload);
      CursorPayload decoded = CursorCodec.decodeAndValidateFilter(encoded, null);
      assertThat(decoded.getFilterHash()).isEqualTo("any");
    }

    @Test
    void empty_expected_hash_skips_check() {
      CursorPayload payload = new CursorPayload(1, Instant.EPOCH, "1", "");
      String encoded = CursorCodec.encode(payload);
      CursorPayload decoded = CursorCodec.decodeAndValidateFilter(encoded, "");
      assertThat(decoded.getFilterHash()).isEmpty();
    }
  }

  @Test
  void encode_rejects_null_payload() {
    assertThatThrownBy(() -> CursorCodec.encode(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("payload");
  }
}
