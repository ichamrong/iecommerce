package com.chamrong.iecommerce.customer;

/** Event published when a customer is blocked. */
public record CustomerBlockedEvent(String tenantId, Long customerId) {}
