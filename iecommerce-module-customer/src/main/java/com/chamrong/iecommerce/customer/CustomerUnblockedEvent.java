package com.chamrong.iecommerce.customer;

/** Event published when a customer is unblocked. */
public record CustomerUnblockedEvent(String tenantId, Long customerId) {}
