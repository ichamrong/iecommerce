package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.domain.TenantPlan;

/**
 * Admin-provisioned tenant creation command.
 *
 * @param shopName Display name of the store
 * @param tenantCode Explicit tenant code chosen by the admin (e.g. "acme_corp")
 * @param ownerEmail Email of the owner — a temporary password will be generated
 * @param plan The billing plan assigned to this tenant
 */
public record TenantProvisionCommand(
    String shopName, String tenantCode, String ownerEmail, TenantPlan plan) {}
