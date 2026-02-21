package com.chamrong.iecommerce.customer.application.dto;

public record AddressResponse(
    Long id,
    String street,
    String city,
    String state,
    String postalCode,
    String country,
    boolean defaultShipping,
    boolean defaultBilling) {}
