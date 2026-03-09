package com.chamrong.iecommerce.ekyc.application;

import com.chamrong.iecommerce.ekyc.application.dto.ApprovalListResponse;
import com.chamrong.iecommerce.ekyc.application.dto.ApprovalResponse;
import com.chamrong.iecommerce.ekyc.application.dto.ReviewRequest;
import com.chamrong.iecommerce.ekyc.domain.EkycApproval;
import com.chamrong.iecommerce.ekyc.domain.EkycStatus;
import com.chamrong.iecommerce.ekyc.domain.RiskScore;
import com.chamrong.iecommerce.ekyc.domain.ports.EkycApprovalRepositoryPort;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for eKYC approvals. */
@Service
@RequiredArgsConstructor
@Slf4j
public class EkycApprovalService {

  private final EkycApprovalRepositoryPort repository;

  @Transactional(readOnly = true)
  public ApprovalListResponse list(String status, String riskScore, int page, int pageSize) {
    EkycStatus s = parseStatus(status);
    RiskScore r = parseRiskScore(riskScore);
    List<EkycApproval> list = repository.findAll(s, r, page, pageSize);
    int total = repository.countAll(s, r);
    List<ApprovalResponse> items = list.stream().map(this::toResponse).toList();
    return new ApprovalListResponse(items, total);
  }

  @Transactional(readOnly = true)
  public Optional<ApprovalResponse> getById(String id) {
    return repository.findById(id).map(this::toResponse);
  }

  @Transactional
  public void review(String id, ReviewRequest request) {
    EkycApproval approval =
        repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Approval not found: " + id));
    if (approval.getStatus() != EkycStatus.PENDING) {
      throw new IllegalStateException("Approval is not pending: " + id);
    }
    approval.setStatus(request.isApproved() ? EkycStatus.APPROVED : EkycStatus.REJECTED);
    if (request.notes() != null) {
      approval.setNotes(request.notes());
    }
    repository.save(approval);
    log.info("eKYC approval {} reviewed: {}", id, request.decision());
  }

  private ApprovalResponse toResponse(EkycApproval a) {
    return new ApprovalResponse(
        a.getId(),
        a.getTenantId(),
        a.getTenantName(),
        a.getOwnerName(),
        a.getDocumentType(),
        a.getDocumentUrl(),
        a.getSubmittedAt(),
        a.getStatus(),
        a.getRiskScore(),
        a.getNotes());
  }

  private static EkycStatus parseStatus(String status) {
    if (status == null || status.isBlank()) return null;
    try {
      return EkycStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static RiskScore parseRiskScore(String riskScore) {
    if (riskScore == null || riskScore.isBlank()) return null;
    try {
      return RiskScore.valueOf(riskScore);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
