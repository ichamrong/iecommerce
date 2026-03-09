package com.chamrong.iecommerce.ekyc.application.dto;

import com.chamrong.iecommerce.ekyc.domain.EkycStatus;
import com.chamrong.iecommerce.ekyc.domain.RiskScore;
import java.time.Instant;

/** API response for a single eKYC approval. */
public record ApprovalResponse(
    String id,
    String tenantId,
    String tenantName,
    String ownerName,
    String documentType,
    String documentUrl,
    Instant submittedAt,
    EkycStatus status,
    RiskScore riskScore,
    String notes) {}
