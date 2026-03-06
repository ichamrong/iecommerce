package com.chamrong.iecommerce.customer.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer_address")
@Getter
@Setter
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

  private String postalCode;

  @Column(nullable = false, columnDefinition = "boolean default false")
  private boolean isDefaultShipping = false;

  @Column(nullable = false, columnDefinition = "boolean default false")
  private boolean isDefaultBilling = false;
}
