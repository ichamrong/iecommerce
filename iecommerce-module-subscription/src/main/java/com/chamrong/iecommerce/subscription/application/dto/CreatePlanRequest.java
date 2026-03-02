package com.chamrong.iecommerce.subscription.application.dto;

import com.chamrong.iecommerce.common.Money;

/**
 * Request payload for creating a new subscription plan.
 *
 * @param code unique business code for the plan
 * @param name human readable plan name
 * @param description marketing description of the plan
 * @param price recurring price for the plan
 * @param maxProducts allowed number of products
 * @param maxOrdersPerMonth allowed number of orders per month
 * @param maxStaffProfiles allowed number of staff profiles
 * @param customDomainAllowed whether custom domains are supported
 * @param active whether the plan is initially active
 */
public record CreatePlanRequest(
    String code,
    String name,
    String description,
    Money price,
    int maxProducts,
    int maxOrdersPerMonth,
    int maxStaffProfiles,
    boolean customDomainAllowed,
    boolean active) {}
