package com.chamrong.iecommerce.chat.domain.policy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ContentPolicyTest {

  @Test
  void validateMessageLength_acceptsWithinLimit() {
    assertThatCode(() -> ContentPolicy.validateMessageLength("hello")).doesNotThrowAnyException();
    assertThatCode(() -> ContentPolicy.validateMessageLength(null)).doesNotThrowAnyException();
  }

  @Test
  void validateMessageLength_rejectsOverLimit() {
    String tooLong = "x".repeat(ContentPolicy.MAX_MESSAGE_LENGTH + 1);
    assertThatThrownBy(() -> ContentPolicy.validateMessageLength(tooLong))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("max length");
  }

  @Test
  void validateAttachmentCount_acceptsWithinLimit() {
    assertThatCode(() -> ContentPolicy.validateAttachmentCount(0)).doesNotThrowAnyException();
    assertThatCode(() -> ContentPolicy.validateAttachmentCount(ContentPolicy.MAX_ATTACHMENTS_PER_MESSAGE))
        .doesNotThrowAnyException();
  }

  @Test
  void validateAttachmentCount_rejectsOverLimit() {
    assertThatThrownBy(() -> ContentPolicy.validateAttachmentCount(ContentPolicy.MAX_ATTACHMENTS_PER_MESSAGE + 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Max");
  }
}
