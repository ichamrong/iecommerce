package com.chamrong.iecommerce.review.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
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
  @Column
  private Integer cleanlinessRating;

  @Column
  private Integer accuracyRating;

  @Column
  private Integer communicationRating;

  @Column
  private Integer locationRating;

  @Column
  private Integer checkInRating;

  @Column
  private Integer valueRating;

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

  @Column
  private boolean flaggedByOwner = false;

  @Column(length = 50)
  private String flagReason; // Spam, Irrelevancy, Harassment, Competitor Conflict

  @Column(columnDefinition = "TEXT")
  private String ownerReply;


  // ── Domain behaviour ───────────────────────────────────────────────────────

  public void approve() {
    this.status = ReviewStatus.APPROVED;
  }

  public void reject() {
    this.status = ReviewStatus.REJECTED;
  }
}
