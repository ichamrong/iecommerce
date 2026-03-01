package com.chamrong.iecommerce.common.security;

import com.chamrong.iecommerce.common.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * IDOR protection: asserts that the loaded resource belongs to the current tenant. Call after
 * loading any tenant-scoped entity by id (e.g. order, payment, invoice) to prevent cross-tenant
 * access.
 *
 * <p>Use when the repository does not already scope by tenantId (e.g. findById only). Prefer
 * repository methods that take tenantId (findByTenantIdAndId) so the guard is redundant but can
 * still be used as a second layer.
 */
public final class TenantGuard {

  private TenantGuard() {}

  /**
   * Ensures tenant context is present. Call before any tenant-scoped operation.
   *
   * @return current tenant id (non-null, non-blank)
   * @throws ResponseStatusException 401 if no tenant in context
   */
  public static String requireTenantIdPresent() {
    String tenantId = TenantContext.getCurrentTenant();
    if (tenantId == null || tenantId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No tenant context");
    }
    return tenantId;
  }

  /**
   * Asserts that the entity's tenant matches the current tenant. Throws 404 so that existence of
   * the resource is not leaked to the caller.
   *
   * @param entityTenantId tenant id on the loaded entity (may be null)
   * @param currentTenantId tenant from TenantContext (must not be null)
   * @throws ResponseStatusException 404 if tenant does not match
   */
  public static void requireSameTenant(String entityTenantId, String currentTenantId) {
    if (currentTenantId == null || currentTenantId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No tenant context");
    }
    if (entityTenantId == null || !entityTenantId.equals(currentTenantId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
    }
  }
}
