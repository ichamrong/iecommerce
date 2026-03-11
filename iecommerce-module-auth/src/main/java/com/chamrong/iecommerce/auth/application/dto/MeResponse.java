package com.chamrong.iecommerce.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Current user info returned by GET /api/v1/auth/me.
 *
 * <p>Used by the frontend to restore session when the app loads with httpOnly cookies (no token in
 * memory). Keeps the user on the dashboard instead of redirecting to login.
 */
public record MeResponse(
    @JsonProperty("user") MeUser user,
    @JsonProperty("requires_password_change") boolean requiresPasswordChange) {

  public record MeUser(
      @JsonProperty("id") String id,
      @JsonProperty("email") String email,
      @JsonProperty("name") String name,
      @JsonProperty("roleId") String roleId,
      @JsonProperty("permissions") List<String> permissions,
      @JsonProperty("tenantId") String tenantId,
      @JsonProperty("sessionId") String sessionId,
      @JsonProperty("assignedTenantIds") List<String> assignedTenantIds) {}
}
