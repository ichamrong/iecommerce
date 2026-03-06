package com.chamrong.iecommerce.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserSessionTest {

  @Test
  void constructorShouldRejectBlankSessionId() {
    assertThatThrownBy(
            () -> new UserSession("  ", "127.0.0.1", "Chrome", Instant.now(), Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("sessionId must not be blank");
  }

  @Test
  void constructorShouldAcceptValidValues() {
    Instant now = Instant.now();

    UserSession session = new UserSession("s1", "127.0.0.1", "Chrome", now.minusSeconds(10), now);

    assertThat(session.sessionId()).isEqualTo("s1");
    assertThat(session.ipAddress()).isEqualTo("127.0.0.1");
    assertThat(session.browser()).isEqualTo("Chrome");
    assertThat(session.startedAt()).isEqualTo(now.minusSeconds(10));
    assertThat(session.lastAccessedAt()).isEqualTo(now);
  }
}
