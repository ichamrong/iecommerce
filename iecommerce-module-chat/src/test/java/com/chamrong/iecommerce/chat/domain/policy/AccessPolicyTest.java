package com.chamrong.iecommerce.chat.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.chat.domain.Conversation;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AccessPolicyTest {

  @Test
  void canRead_whenParticipant_returnsTrue() {
    Conversation c = new Conversation("t1", Set.of(1L, 2L));
    assertThat(AccessPolicy.canRead(c, 1L, false)).isTrue();
    assertThat(AccessPolicy.canRead(c, 2L, false)).isTrue();
  }

  @Test
  void canRead_whenNotParticipantAndNotStaff_returnsFalse() {
    Conversation c = new Conversation("t1", Set.of(1L));
    assertThat(AccessPolicy.canRead(c, 99L, false)).isFalse();
  }

  @Test
  void canRead_whenStaff_returnsTrueEvenIfNotParticipant() {
    Conversation c = new Conversation("t1", Set.of(1L));
    assertThat(AccessPolicy.canRead(c, 99L, true)).isTrue();
  }

  @Test
  void canSend_whenParticipant_returnsTrue() {
    Conversation c = new Conversation("t1", Set.of(1L));
    assertThat(AccessPolicy.canSend(c, 1L)).isTrue();
  }

  @Test
  void canSend_whenNotParticipant_returnsFalse() {
    Conversation c = new Conversation("t1", Set.of(1L));
    assertThat(AccessPolicy.canSend(c, 99L)).isFalse();
  }
}
