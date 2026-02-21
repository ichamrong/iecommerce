package com.chamrong.iecommerce.auth.api;

import com.chamrong.iecommerce.auth.application.command.TenantProvisionCommand;
import com.chamrong.iecommerce.auth.application.command.TenantProvisionHandler;
import com.chamrong.iecommerce.auth.application.command.TenantSignupCommand;
import com.chamrong.iecommerce.auth.application.command.TenantSignupHandler;
import com.chamrong.iecommerce.auth.application.command.UpdateTenantPreferencesCommand;
import com.chamrong.iecommerce.auth.application.command.UpdateTenantPreferencesHandler;
import com.chamrong.iecommerce.auth.application.command.UpdateTenantStatusCommand;
import com.chamrong.iecommerce.auth.application.command.UpdateTenantStatusHandler;
import com.chamrong.iecommerce.auth.application.dto.TenantPreferencesResponse;
import com.chamrong.iecommerce.auth.application.dto.TenantResponse;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.application.query.GetTenantPreferencesHandler;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.common.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Tenant provisioning endpoints for both self-service and admin-managed flows. */
@RestController
public class TenantController {

  private final TenantSignupHandler signupHandler;
  private final TenantProvisionHandler provisionHandler;
  private final UpdateTenantStatusHandler statusHandler;
  private final UpdateTenantPreferencesHandler updatePreferencesHandler;
  private final GetTenantPreferencesHandler getPreferencesHandler;

  public TenantController(
      TenantSignupHandler signupHandler,
      TenantProvisionHandler provisionHandler,
      UpdateTenantStatusHandler statusHandler,
      UpdateTenantPreferencesHandler updatePreferencesHandler,
      GetTenantPreferencesHandler getPreferencesHandler) {
    this.signupHandler = signupHandler;
    this.provisionHandler = provisionHandler;
    this.statusHandler = statusHandler;
    this.updatePreferencesHandler = updatePreferencesHandler;
    this.getPreferencesHandler = getPreferencesHandler;
  }

  /** Self-service tenant registration — public, no auth required. */
  @PostMapping("/api/v1/tenants/register")
  public ResponseEntity<?> selfServiceSignup(@RequestBody TenantSignupCommand cmd) {
    try {
      TenantResponse response = signupHandler.handle(cmd);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (DuplicateUserException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
  }

  /** Admin-provisioned tenant creation. */
  @PostMapping("/api/v1/admin/tenants")
  @PreAuthorize(Permissions.HAS_TENANT_CREATE)
  public ResponseEntity<?> adminProvision(@RequestBody TenantProvisionCommand cmd) {
    try {
      TenantResponse response = provisionHandler.handle(cmd);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (DuplicateUserException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
  }

  /** Updates a tenant's billing/operational status. */
  @PutMapping("/api/v1/admin/tenants/{id}/status")
  @PreAuthorize(Permissions.HAS_TENANT_CREATE)
  public ResponseEntity<?> updateStatus(
      @PathVariable("id") String tenantId, @RequestBody UpdateTenantStatusCommand cmd) {
    try {
      statusHandler.handle(new UpdateTenantStatusCommand(tenantId, cmd.status()));
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Update the current tenant's storefront preferences. Only accessible by users authenticated
   * within the tenant context.
   */
  @PutMapping("/api/v1/tenants/me/preferences")
  public ResponseEntity<TenantPreferencesResponse> updateMyPreferences(
      @RequestBody UpdateTenantPreferencesCommand body) {
    String currentTenantId = TenantContext.getCurrentTenant();
    if (currentTenantId == null || "SYSTEM".equals(currentTenantId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    UpdateTenantPreferencesCommand cmd =
        new UpdateTenantPreferencesCommand(
            currentTenantId,
            body.logoUrl(),
            body.primaryColor(),
            body.secondaryColor(),
            body.fontFamily());

    return ResponseEntity.ok(updatePreferencesHandler.handle(cmd));
  }

  /** Public endpoint to fetch a tenant's storefront preferences for dynamic UI branding. */
  @GetMapping("/api/v1/storefront/{tenantId}/preferences")
  public ResponseEntity<TenantPreferencesResponse> getStorefrontPreferences(
      @PathVariable("tenantId") String tenantId) {
    try {
      return ResponseEntity.ok(getPreferencesHandler.handle(tenantId));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
