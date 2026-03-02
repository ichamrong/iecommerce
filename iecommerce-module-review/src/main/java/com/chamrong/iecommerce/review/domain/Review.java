package com.chamrong.iecommerce.review.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.review.domain.exception.ReviewDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_review")
public class Review extends BaseTenantEntity {

  @Column(nullable = false)
  private Long productId;

  @Column(nullable = false)
  private Long customerId;

  @Column(nullable = false)
  private Long bookingId; // 1 review per booking idempotency

  @Column(nullable = false)
  private boolean isAnonymous = false;

  /** Overall Star rating 1–5. */
  @Column(nullable = false)
  private Integer rating;

  // ── Granular Ratings (Optional but recommended) ────────────────────────────
  @Column private Integer cleanlinessRating;

  @Column private Integer accuracyRating;

  @Column private Integer communicationRating;

  @Column private Integer locationRating;

  @Column private Integer checkInRating;

  @Column private Integer valueRating;

  // ── Text & Media ───────────────────────────────────────────────────────────
  @Column(columnDefinition = "TEXT")
  private String comment;

  /** CSV list of up to 3 image keys stored in S3/R2 */
  @Column(length = 500)
  private String mediaKeys;

  // ── Moderation & Disputes ──────────────────────────────────────────────────
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReviewStatus status = ReviewStatus.PENDING;

  @Column private boolean flaggedByOwner = false;

  @Column(length = 50)
  private String flagReason; // Spam, Irrelevancy, Harassment, Competitor Conflict

  @Column(columnDefinition = "TEXT")
  private String ownerReply;

  // ── Domain behaviour ───────────────────────────────────────────────────────

  public void approve() {
    assertNotDeleted("approve");
    this.status = ReviewStatus.APPROVED;
  }

  public void reject() {
    assertNotDeleted("reject");
    this.status = ReviewStatus.REJECTED;
  }

  public void hide() {
    assertNotDeleted("hide");
    this.status = ReviewStatus.HIDDEN;
  }

  public void softDelete() {
    if (this.status == ReviewStatus.DELETED) {
      return;
    }
    this.status = ReviewStatus.DELETED;
  }

  public void flagByOwner(String reason) {
    assertNotDeleted("flag");
    if (reason == null || reason.isBlank()) {
      throw new ReviewDomainException("Flag reason must not be blank");
    }
    this.flaggedByOwner = true;
    this.flagReason = reason;
    this.status = ReviewStatus.PENDING;
  }

  public void replyAsOwner(String reply) {
    assertNotDeleted("reply to");
    if (reply == null || reply.isBlank()) {
      throw new ReviewDomainException("Reply must not be blank");
    }
    this.ownerReply = reply;
  }

  private void assertNotDeleted(String action) {
    if (this.status == ReviewStatus.DELETED) {
      throw new ReviewDomainException(
          "Cannot " + action + " a deleted review (id=" + getId() + ")");
    }
  }
}
