package com.chamrong.iecommerce.review.domain.model;

import com.chamrong.iecommerce.review.domain.ReviewStatus;
import java.time.Instant;

/**
 * Aggregate root for reviews.
 *
 * <p>Represents a single review on a target (product, order, booking, store, staff, etc.) and
 * encapsulates moderation and lifecycle behavior.
 */
public final class Review {

  private final Long id;
  private final TenantScope tenantScope;
  private final ReviewTarget target;
  private final String targetId;
  private final String authorId;
  private final boolean verified;
  private Rating rating;
  private String title;
  private String body;
  private ReviewStatus status;
  private final Instant createdAt;
  private Instant updatedAt;
  private boolean flaggedByOwner;
  private String flagReason;
  private String ownerReply;

  public Review(
      Long id,
      TenantScope tenantScope,
      ReviewTarget target,
      String targetId,
      String authorId,
      Rating rating,
      boolean verified,
      String title,
      String body,
      ReviewStatus status,
      Instant createdAt,
      Instant updatedAt,
      boolean flaggedByOwner,
      String flagReason,
      String ownerReply) {
    if (tenantScope == null) {
      throw new IllegalArgumentException("tenantScope must not be null");
    }
    if (target == null) {
      throw new IllegalArgumentException("target must not be null");
    }
    if (targetId == null || targetId.isBlank()) {
      throw new IllegalArgumentException("targetId must not be blank");
    }
    if (authorId == null || authorId.isBlank()) {
      throw new IllegalArgumentException("authorId must not be blank");
    }
    if (rating == null) {
      throw new IllegalArgumentException("rating must not be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("status must not be null");
    }
    if (createdAt == null) {
      throw new IllegalArgumentException("createdAt must not be null");
    }
    this.id = id;
    this.tenantScope = tenantScope;
    this.target = target;
    this.targetId = targetId;
    this.authorId = authorId;
    this.rating = rating;
    this.verified = verified;
    this.title = title;
    this.body = body;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt != null ? updatedAt : createdAt;
    this.flaggedByOwner = flaggedByOwner;
    this.flagReason = flagReason;
    this.ownerReply = ownerReply;
  }

  public Long getId() {
    return id;
  }

  public TenantScope getTenantScope() {
    return tenantScope;
  }

  public ReviewTarget getTarget() {
    return target;
  }

  public String getTargetId() {
    return targetId;
  }

  public String getAuthorId() {
    return authorId;
  }

  public Rating getRating() {
    return rating;
  }

  public boolean isVerified() {
    return verified;
  }

  public String getTitle() {
    return title;
  }

  public String getBody() {
    return body;
  }

  public ReviewStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public boolean isFlaggedByOwner() {
    return flaggedByOwner;
  }

  public String getFlagReason() {
    return flagReason;
  }

  public String getOwnerReply() {
    return ownerReply;
  }

  public void updateContent(String newTitle, String newBody, Rating newRating, Instant now) {
    if (status == ReviewStatus.DELETED) {
      throw new IllegalStateException("Cannot update deleted review");
    }
    this.title = newTitle;
    this.body = newBody;
    this.rating = newRating;
    this.updatedAt = now;
  }

  public void approve() {
    this.status = ReviewStatus.APPROVED;
  }

  public void reject() {
    this.status = ReviewStatus.REJECTED;
  }

  public void hide() {
    this.status = ReviewStatus.HIDDEN;
  }

  public void softDelete() {
    this.status = ReviewStatus.DELETED;
  }

  public void flagByOwner(String reason) {
    this.flaggedByOwner = true;
    this.flagReason = reason;
  }

  public void replyAsOwner(String reply, Instant now) {
    this.ownerReply = reply;
    this.updatedAt = now;
  }
}
