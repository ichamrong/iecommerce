package com.chamrong.iecommerce.staff.application.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chamrong.iecommerce.staff.infrastructure.persistence.StaffCursorDecoded;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class StaffCursorEncoderTest {

  @Test
  void shouldEncodeAndDecodeRoundTrip() {
    Instant now = Instant.ofEpochMilli(1735000000000L);
    Long id = 42L;

    String token = StaffCursorEncoder.encode(now, id);
    assertNotNull(token);
    assertFalse(token.isBlank());

    StaffCursorDecoded decoded = StaffCursorEncoder.decode(token);
    assertNotNull(decoded);
    assertEquals(now, decoded.createdAt());
    assertEquals(id, decoded.id());
  }

  @Test
  void shouldReturnNullForNullCursor() {
    assertNull(StaffCursorEncoder.decode(null));
  }

  @Test
  void shouldReturnNullForBlankCursor() {
    assertNull(StaffCursorEncoder.decode("  "));
  }

  @Test
  void shouldThrowForMalformedCursor() {
    assertThrows(
        IllegalArgumentException.class, () -> StaffCursorEncoder.decode("notBase64Cursor!!"));
  }
}
