package com.chamrong.iecommerce.auth.application.dto;

import com.chamrong.iecommerce.auth.domain.TenantPlan;
import com.chamrong.iecommerce.auth.domain.TenantStatus;

/**
 * Response returned after tenant provisioning.
 *
 * @param tenantCode The unique code identifying the tenant (used as tenantId everywhere)
 * @param shopName Display name of the store
 * @param plan Billing plan
 * @param status Current lifecycle status of the tenant
 * @param ownerEmail Email address of the owner account
 * @param tempPassword Non-null only for admin-provisioned tenants — share with the owner
 *     out-of-band
 */
public record TenantResponse(
    String tenantCode,
    String shopName,
    TenantPlan plan,
    TenantStatus status,
    String ownerEmail,
    String tempPassword) {}
