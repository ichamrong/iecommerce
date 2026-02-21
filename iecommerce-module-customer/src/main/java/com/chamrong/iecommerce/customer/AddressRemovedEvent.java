package com.chamrong.iecommerce.customer;

/** Event published when an address is removed from a customer. */
public record AddressRemovedEvent(String tenantId, Long customerId, Long addressId) {}
