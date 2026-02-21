package com.chamrong.iecommerce.auth;

import java.util.Set;

/**
 * Event published when a platform staff member's assigned scope of tenants is updated.
 *
 * @param username the staff's unique username (corresponding to Keycloak user)
 * @param tenantCodes the list of tenant codes this staff member can access
 */
public record StaffTenantsUpdatedEvent(String username, Set<String> tenantCodes) {}
