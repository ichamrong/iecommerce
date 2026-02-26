package com.chamrong.iecommerce.review.api;

import com.chamrong.iecommerce.review.application.ReviewService;
import com.chamrong.iecommerce.review.application.dto.ReviewRequest;
import com.chamrong.iecommerce.review.application.dto.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Product review submission and moderation.
 *
 * <p>Base path: {@code /api/v1/reviews}
 */
@Tag(name = "Reviews", description = "Product review submission and moderation")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @Operation(summary = "Get approved reviews for a product")
  @GetMapping("/products/{productId}")
  public List<ReviewResponse> getProductReviews(@PathVariable Long productId) {
    return reviewService.getApprovedReviews(productId);
  }

  @Operation(summary = "Submit a product review")
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ReviewResponse> submit(@RequestBody ReviewRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.submit(req));
  }

  @Operation(
      summary = "Approve a review",
      description = "Moderator-only — makes review visible on storefront.")
  @PostMapping("/{id}/approve")
  @PreAuthorize("hasAuthority('reviews:moderate')")
  public ReviewResponse approve(@PathVariable Long id) {
    return reviewService.approve(id);
  }

  @Operation(
      summary = "Reject a review",
      description = "Moderator-only — hides review from storefront.")
  @PostMapping("/{id}/reject")
  @PreAuthorize("hasAuthority('reviews:moderate')")
  public ReviewResponse reject(@PathVariable Long id) {
    return reviewService.reject(id);
  }

  @Operation(
      summary = "Get pending reviews",
      description = "Returns all reviews awaiting moderation.")
  @GetMapping("/pending")
  @PreAuthorize("hasAuthority('reviews:moderate')")
  public List<ReviewResponse> getPending() {
    return reviewService.getPendingReviews();
  }

  @Operation(
      summary = "Flag a review",
      description = "Owner can flag a review for moderation review.")
  @PostMapping("/{id}/flag")
  @PreAuthorize("hasAuthority('reviews:manage')")
  public ReviewResponse flagReview(@PathVariable Long id, @RequestParam String reason) {
    return reviewService.flagReview(id, reason);
  }

  @Operation(summary = "Reply to a review", description = "Owner can post a public reply.")
  @PostMapping("/{id}/reply")
  @PreAuthorize("hasAuthority('reviews:manage')")
  public ReviewResponse replyToReview(@PathVariable Long id, @RequestBody String reply) {
    return reviewService.replyToReview(id, reply);
  }
}
