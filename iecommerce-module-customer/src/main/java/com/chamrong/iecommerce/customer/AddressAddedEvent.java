package com.chamrong.iecommerce.customer;

/** Event published when an address is added to a customer. */
public record AddressAddedEvent(String tenantId, Long customerId, String fullAddress) {}
