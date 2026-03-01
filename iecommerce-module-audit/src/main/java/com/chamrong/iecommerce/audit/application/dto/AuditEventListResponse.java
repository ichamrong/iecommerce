package com.chamrong.iecommerce.audit.application.dto;

import com.chamrong.iecommerce.common.pagination.CursorPageResponse;

/**
 * Cursor-paginated list of audit events. Wrapper around CursorPageResponse&lt;AuditEventResponse&gt;.
 */
public record AuditEventListResponse(CursorPageResponse<AuditEventResponse> page) {

  public static AuditEventListResponse of(CursorPageResponse<AuditEventResponse> page) {
    return new AuditEventListResponse(page);
  }
}
