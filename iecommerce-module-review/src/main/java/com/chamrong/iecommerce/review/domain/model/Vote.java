package com.chamrong.iecommerce.review.domain.model;

import java.time.Instant;

/** Helpful/unhelpful vote on a review. */
public final class Vote {

  private final Long id;
  private final TenantScope tenantScope;
  private final Long reviewId;
  private final String voterId;
  private final boolean helpful;
  private final Instant createdAt;

  public Vote(
      Long id,
      TenantScope tenantScope,
      Long reviewId,
      String voterId,
      boolean helpful,
      Instant createdAt) {
    if (tenantScope == null) {
      throw new IllegalArgumentException("tenantScope must not be null");
    }
    if (reviewId == null) {
      throw new IllegalArgumentException("reviewId must not be null");
    }
    if (voterId == null || voterId.isBlank()) {
      throw new IllegalArgumentException("voterId must not be blank");
    }
    if (createdAt == null) {
      throw new IllegalArgumentException("createdAt must not be null");
    }
    this.id = id;
    this.tenantScope = tenantScope;
    this.reviewId = reviewId;
    this.voterId = voterId;
    this.helpful = helpful;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public TenantScope getTenantScope() {
    return tenantScope;
  }

  public Long getReviewId() {
    return reviewId;
  }

  public String getVoterId() {
    return voterId;
  }

  public boolean isHelpful() {
    return helpful;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
