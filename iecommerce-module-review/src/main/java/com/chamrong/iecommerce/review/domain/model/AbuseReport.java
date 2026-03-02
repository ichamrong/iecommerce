package com.chamrong.iecommerce.review.domain.model;

import java.time.Instant;

/** Abuse report raised against a review. */
public final class AbuseReport {

  private final Long id;
  private final TenantScope tenantScope;
  private final Long reviewId;
  private final String reporterId;
  private final String reason;
  private final Instant createdAt;

  public AbuseReport(
      Long id,
      TenantScope tenantScope,
      Long reviewId,
      String reporterId,
      String reason,
      Instant createdAt) {
    if (tenantScope == null) {
      throw new IllegalArgumentException("tenantScope must not be null");
    }
    if (reviewId == null) {
      throw new IllegalArgumentException("reviewId must not be null");
    }
    if (reporterId == null || reporterId.isBlank()) {
      throw new IllegalArgumentException("reporterId must not be blank");
    }
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("reason must not be blank");
    }
    if (createdAt == null) {
      throw new IllegalArgumentException("createdAt must not be null");
    }
    this.id = id;
    this.tenantScope = tenantScope;
    this.reviewId = reviewId;
    this.reporterId = reporterId;
    this.reason = reason;
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

  public String getReporterId() {
    return reporterId;
  }

  public String getReason() {
    return reason;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
