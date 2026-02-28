package com.chamrong.iecommerce.auth.api;

import com.chamrong.iecommerce.auth.application.command.TenantProvisionCommand;
import com.chamrong.iecommerce.auth.application.command.TenantSignupCommand;
import com.chamrong.iecommerce.auth.application.command.UpdateTenantPreferencesCommand;
import com.chamrong.iecommerce.auth.application.command.UpdateTenantStatusCommand;
import com.chamrong.iecommerce.auth.application.command.tenant.TenantProvisionHandler;
import com.chamrong.iecommerce.auth.application.command.tenant.TenantSignupHandler;
import com.chamrong.iecommerce.auth.application.command.tenant.UpdateTenantPreferencesHandler;
import com.chamrong.iecommerce.auth.application.command.tenant.UpdateTenantStatusHandler;
import com.chamrong.iecommerce.auth.application.dto.TenantPreferencesResponse;
import com.chamrong.iecommerce.auth.application.dto.TenantResponse;
import com.chamrong.iecommerce.auth.application.query.GetTenantPreferencesHandler;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.common.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(
    name = "Tenants",
    description = "Tenant provisioning, status management and storefront preferences")
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
  @Operation(
      summary = "Self-service tenant registration",
      description =
          "Public endpoint. Registers a new tenant and their admin user. No authentication"
              + " required.")
  @PostMapping("/api/v1/tenants/register")
  public ResponseEntity<TenantResponse> selfServiceSignup(
      @Valid @RequestBody TenantSignupCommand cmd) {
    TenantResponse response = signupHandler.handle(cmd);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /** Admin-provisioned tenant creation. */
  @Operation(
      summary = "Admin: provision a tenant",
      description = "Admin-only. Creates a new tenant. Requires `tenant:create` permission.")
  @PostMapping("/api/v1/admin/tenants")
  @PreAuthorize(Permissions.HAS_TENANT_CREATE)
  public ResponseEntity<TenantResponse> adminProvision(
      @Valid @RequestBody TenantProvisionCommand cmd) {
    TenantResponse response = provisionHandler.handle(cmd);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /** Updates a tenant's billing/operational status. */
  @Operation(
      summary = "Admin: update tenant status",
      description =
          "Update a tenant's billing/operational status (e.g. ACTIVE, SUSPENDED). Requires"
              + " `tenant:create` permission.")
  @PutMapping("/api/v1/admin/tenants/{id}/status")
  @PreAuthorize(Permissions.HAS_TENANT_CREATE)
  public ResponseEntity<Void> updateStatus(
      @PathVariable("id") String tenantId, @RequestBody UpdateTenantStatusCommand cmd) {
    try {
      statusHandler.handle(new UpdateTenantStatusCommand(tenantId, cmd.status()));
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      // Future-proof: We can map this to AuthException(TENANT_NOT_FOUND) later
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Update the current tenant's storefront preferences. Only accessible by users authenticated
   * within the tenant context.
   */
  @Operation(
      summary = "Update my storefront preferences",
      description = "Updates the current tenant's branding (logo, colors, font). Requires JWT.")
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
  @Operation(
      summary = "Get storefront preferences",
      description =
          "Public endpoint. Returns the branding/preferences for a given tenant's storefront.")
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
