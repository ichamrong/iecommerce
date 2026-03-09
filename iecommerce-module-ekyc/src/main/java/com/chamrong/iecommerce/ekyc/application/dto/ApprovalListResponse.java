package com.chamrong.iecommerce.ekyc.application.dto;

import java.util.List;

/** API response for paginated list of eKYC approvals. */
public record ApprovalListResponse(List<ApprovalResponse> items, int total) {}
