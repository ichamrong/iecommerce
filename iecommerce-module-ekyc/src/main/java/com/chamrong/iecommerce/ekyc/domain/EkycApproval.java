package com.chamrong.iecommerce.ekyc.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Domain model for an eKYC / merchant approval. */
@Getter
@Setter
@Builder
public class EkycApproval {

  private String id;
  private String tenantId;
  private String tenantName;
  private String ownerName;
  private String documentType;
  private String documentUrl;
  private Instant submittedAt;
  private EkycStatus status;
  private RiskScore riskScore;
  private String notes;
}
