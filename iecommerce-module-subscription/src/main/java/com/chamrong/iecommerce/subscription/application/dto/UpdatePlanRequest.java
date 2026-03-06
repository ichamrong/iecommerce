package com.chamrong.iecommerce.subscription.application.dto;

import com.chamrong.iecommerce.common.Money;

/**
 * Request payload for updating an existing subscription plan.
 *
 * @param name human readable plan name
 * @param description marketing description of the plan
 * @param price recurring price for the plan
 * @param maxProducts allowed number of products
 * @param maxVariants allowed number of product variants
 * @param maxOrdersPerMonth allowed number of orders per month
 * @param maxStaffProfiles allowed number of staff profiles
 * @param customDomainAllowed whether custom domains are supported
 * @param active whether the plan is active
 */
public record UpdatePlanRequest(
    String name,
    String description,
    Money price,
    int maxProducts,
    int maxVariants,
    int maxOrdersPerMonth,
    int maxStaffProfiles,
    boolean customDomainAllowed,
    boolean active) {}
