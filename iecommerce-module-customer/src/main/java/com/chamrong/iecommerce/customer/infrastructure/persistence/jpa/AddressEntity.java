package com.chamrong.iecommerce.customer.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_address")
public class AddressEntity extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private CustomerEntity customer;

  @Column(nullable = false)
  private String street;

  @Column(nullable = false)
  private String city;

  private String state;

  @Column(nullable = false)
  private String country;

  @Column(name = "postal_code")
  private String postalCode;

  @Column(name = "is_default_shipping", nullable = false)
  private boolean defaultShipping = false;

  @Column(name = "is_default_billing", nullable = false)
  private boolean defaultBilling = false;

  public CustomerEntity getCustomer() {
    return customer;
  }

  public void setCustomer(CustomerEntity customer) {
    this.customer = customer;
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
}
