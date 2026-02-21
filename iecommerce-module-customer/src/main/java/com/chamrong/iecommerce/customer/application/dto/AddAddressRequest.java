package com.chamrong.iecommerce.customer.application.dto;

public record AddAddressRequest(
    String street,
    String city,
    String state,
    String postalCode,
    String country,
    boolean isDefaultShipping,
    boolean isDefaultBilling) {}
