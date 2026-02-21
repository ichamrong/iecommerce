package com.chamrong.iecommerce.customer.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_address")
@Getter
@Setter
@NoArgsConstructor
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
}
