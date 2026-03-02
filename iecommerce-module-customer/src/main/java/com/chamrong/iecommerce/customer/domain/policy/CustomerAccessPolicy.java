package com.chamrong.iecommerce.customer.domain.policy;

/**
 * IDOR and access rules: verify resource belongs to current tenant and optionally to current
 * customer.
 */
public final class CustomerAccessPolicy {

  private CustomerAccessPolicy() {}

  /**
   * Verifies that the resource's tenant matches the current tenant. Throw if not.
   *
   * @param resourceTenantId tenant of the loaded resource
   * @param currentTenantId tenant from context (e.g. JWT)
   */
  public static void requireSameTenant(String resourceTenantId, String currentTenantId) {
    if (resourceTenantId == null || !resourceTenantId.equals(currentTenantId)) {
      throw new SecurityException("Access denied: tenant mismatch");
    }
  }

  /**
   * Verifies that the customer belongs to the current tenant and (optionally) is the current
   * customer for self-service.
   */
  public static void requireCustomerAccess(
      String resourceTenantId,
      String currentTenantId,
      Long resourceCustomerId,
      Long currentCustomerId) {
    requireSameTenant(resourceTenantId, currentTenantId);
    if (currentCustomerId != null && !currentCustomerId.equals(resourceCustomerId)) {
      throw new SecurityException("Access denied: customer mismatch");
    }
  }
}
