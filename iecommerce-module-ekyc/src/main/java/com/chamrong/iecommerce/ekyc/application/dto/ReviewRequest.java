package com.chamrong.iecommerce.ekyc.application.dto;

import jakarta.validation.constraints.NotNull;

/** Request body for POST /api/v1/ekyc/approvals/:id/review. */
public record ReviewRequest(
    @NotNull(message = "decision is required") String decision, String notes) {

  public boolean isApproved() {
    return "approved".equalsIgnoreCase(decision);
  }
}
