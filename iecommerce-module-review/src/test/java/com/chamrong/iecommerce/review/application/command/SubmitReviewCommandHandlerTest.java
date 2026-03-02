package com.chamrong.iecommerce.review.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.event.ReviewSubmittedEvent;
import com.chamrong.iecommerce.review.application.ReviewOutboxPublisher;
import com.chamrong.iecommerce.review.application.ReviewResponseMapper;
import com.chamrong.iecommerce.review.application.dto.ReviewRequest;
import com.chamrong.iecommerce.review.application.dto.ReviewResponse;
import com.chamrong.iecommerce.review.domain.Review;
import com.chamrong.iecommerce.review.domain.ReviewRepository;
import com.chamrong.iecommerce.review.domain.ReviewStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class SubmitReviewCommandHandlerTest {

  private final ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
  private final ReviewResponseMapper mapper = new ReviewResponseMapper();
  private final ReviewOutboxPublisher outboxPublisher = Mockito.mock(ReviewOutboxPublisher.class);

  private SubmitReviewCommandHandler handler;

  @BeforeEach
  void setUp() {
    handler = new SubmitReviewCommandHandler(reviewRepository, mapper, outboxPublisher);
    TenantContext.setCurrentTenant("tenant-1");
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void rejectsNullRating() {
    ReviewRequest request =
        new ReviewRequest(
            1L, 2L, 3L, false, null, null, null, null, null, null, null, "comment", null);

    assertThatThrownBy(() -> handler.handle(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Rating must not be null");
  }

  @Test
  void usesRatingValueObjectAndPublishesOutboxEvent() {
    ReviewRequest request =
        new ReviewRequest(1L, 2L, 3L, false, 5, 4, 4, 5, 5, 5, 4, "Great stay", "img1,img2");

    Review persisted = new Review();
    persisted.setId(10L);
    persisted.setTenantId("tenant-1");
    persisted.setProductId(1L);
    persisted.setCustomerId(2L);
    persisted.setBookingId(3L);
    persisted.setRating(5);
    persisted.setStatus(ReviewStatus.PENDING);

    when(reviewRepository.save(any(Review.class))).thenReturn(persisted);

    ReviewResponse response = handler.handle(request);

    assertThat(response.id()).isEqualTo(10L);
    assertThat(response.rating()).isEqualTo(5);

    ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
    verify(outboxPublisher).publish(eq("tenant-1"), eventCaptor.capture());
    assertThat(eventCaptor.getValue()).isInstanceOf(ReviewSubmittedEvent.class);
  }
}
