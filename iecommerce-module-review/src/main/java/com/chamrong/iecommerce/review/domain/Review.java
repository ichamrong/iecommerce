package com.chamrong.iecommerce.review.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_review")
public class Review extends BaseTenantEntity {

  @Column(nullable = false)
  private Long productId;

  @Column(nullable = false)
  private Long customerId;

  @Column(nullable = false)
  private Integer rating; // 1-5

  @Column(columnDefinition = "TEXT")
  private String comment;

  @Column(nullable = false)
  private String status = "PENDING"; // PENDING, APPROVED, REJECTED

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long customerId) {
    this.customerId = customerId;
  }

  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer rating) {
    this.rating = rating;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
