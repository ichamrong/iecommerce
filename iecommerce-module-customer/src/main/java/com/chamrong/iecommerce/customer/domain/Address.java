package com.chamrong.iecommerce.customer.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_address")
public class Address extends BaseEntity {

  @Column(nullable = false)
  private String street;

  @Column(nullable = false)
  private String city;

  private String state;

  @Column(nullable = false)
  private String country;

  private String postalCode;

  private boolean isDefaultShipping = false;
  private boolean isDefaultBilling = false;

  public Address() {}

  public Address(String street, String city, String country) {
    this.street = street;
    this.city = city;
    this.country = country;
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
    return isDefaultShipping;
  }

  public void setDefaultShipping(boolean defaultShipping) {
    isDefaultShipping = defaultShipping;
  }

  public boolean isDefaultBilling() {
    return isDefaultBilling;
  }

  public void setDefaultBilling(boolean defaultBilling) {
    isDefaultBilling = defaultBilling;
  }

  // ── Domain behaviour ─────────────────────────────────────────────────────

  public void markAsDefaultShipping() {
    this.isDefaultShipping = true;
  }

  public void clearDefaultShipping() {
    this.isDefaultShipping = false;
  }

  public void markAsDefaultBilling() {
    this.isDefaultBilling = true;
  }

  public void clearDefaultBilling() {
    this.isDefaultBilling = false;
  }
}
