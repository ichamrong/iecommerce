package com.chamrong.iecommerce.customer.domain.model;

import java.util.Objects;

/**
 * Address value object / entity within the customer aggregate. No JPA; infrastructure maps to
 * persistence.
 */
public class Address {

  private Long id;
  private String street;
  private String city;
  private String state;
  private String country;
  private String postalCode;
  private boolean defaultShipping;
  private boolean defaultBilling;

  public Address() {}

  public Address(String street, String city, String country) {
    this.street = street;
    this.city = city;
    this.country = country;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public boolean isDefaultShipping() {
    return defaultShipping;
  }

  public void setDefaultShipping(boolean defaultShipping) {
    this.defaultShipping = defaultShipping;
  }

  public boolean isDefaultBilling() {
    return defaultBilling;
  }

  public void setDefaultBilling(boolean defaultBilling) {
    this.defaultBilling = defaultBilling;
  }

  public void markAsDefaultShipping() {
    this.defaultShipping = true;
  }

  public void clearDefaultShipping() {
    this.defaultShipping = false;
  }

  public void markAsDefaultBilling() {
    this.defaultBilling = true;
  }

  public void clearDefaultBilling() {
    this.defaultBilling = false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Address address = (Address) o;
    return Objects.equals(id, address.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
