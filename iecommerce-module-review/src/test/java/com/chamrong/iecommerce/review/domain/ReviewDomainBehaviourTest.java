package com.chamrong.iecommerce.review.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chamrong.iecommerce.review.domain.exception.ReviewDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReviewDomainBehaviourTest {

  @Nested
  @DisplayName("approve/reject/hide/softDelete")
  class Lifecycle {

    @Test
    void preventsMutationsWhenDeleted() {
      Review review = new Review();
      review.setStatus(ReviewStatus.DELETED);

      assertThatThrownBy(review::approve)
          .isInstanceOf(ReviewDomainException.class)
          .hasMessageContaining("deleted review");
      assertThatThrownBy(review::reject)
          .isInstanceOf(ReviewDomainException.class)
          .hasMessageContaining("deleted review");
      assertThatThrownBy(review::hide)
          .isInstanceOf(ReviewDomainException.class)
          .hasMessageContaining("deleted review");
    }

    @Test
    void softDeleteIsIdempotent() {
      Review review = new Review();
      review.setStatus(ReviewStatus.APPROVED);

      review.softDelete();
      ReviewStatus first = review.getStatus();

      review.softDelete();
      ReviewStatus second = review.getStatus();

      assertThat(first).isEqualTo(ReviewStatus.DELETED);
      assertThat(second).isEqualTo(ReviewStatus.DELETED);
    }
  }

  @Nested
  @DisplayName("flagByOwner")
  class FlagByOwner {

    @Test
    void requiresNonBlankReason() {
      Review review = new Review();

      assertThatThrownBy(() -> review.flagByOwner(null))
          .isInstanceOf(ReviewDomainException.class)
          .hasMessageContaining("Flag reason");
      assertThatThrownBy(() -> review.flagByOwner("   "))
          .isInstanceOf(ReviewDomainException.class)
          .hasMessageContaining("Flag reason");
    }

    @Test
    void setsFlaggedAttributesAndResetsStatusToPending() {
      Review review = new Review();
      review.setStatus(ReviewStatus.APPROVED);

      review.flagByOwner("Spam");

      assertThat(review.isFlaggedByOwner()).isTrue();
      assertThat(review.getFlagReason()).isEqualTo("Spam");
      assertThat(review.getStatus()).isEqualTo(ReviewStatus.PENDING);
    }
  }

  @Nested
  @DisplayName("replyAsOwner")
  class ReplyAsOwner {

    @Test
    void requiresNonBlankReply() {
      Review review = new Review();

      assertThatThrownBy(() -> review.replyAsOwner(null))
          .isInstanceOf(ReviewDomainException.class)
          .hasMessageContaining("Reply must not be blank");
      assertThatThrownBy(() -> review.replyAsOwner(" "))
          .isInstanceOf(ReviewDomainException.class)
          .hasMessageContaining("Reply must not be blank");
    }

    @Test
    void storesReplyText() {
      Review review = new Review();

      review.replyAsOwner("Thank you!");

      assertThat(review.getOwnerReply()).isEqualTo("Thank you!");
    }
  }
}
