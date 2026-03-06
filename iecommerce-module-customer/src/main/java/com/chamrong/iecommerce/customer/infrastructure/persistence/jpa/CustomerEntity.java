package com.chamrong.iecommerce.customer.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "customer",
    indexes = {
      @Index(name = "idx_customer_keyset", columnList = "tenant_id, created_at DESC, id DESC")
    })
public class CustomerEntity extends BaseTenantEntity {

  private String firstName;

  private String lastName;

  @Column(nullable = false)
  private String email;

  private String phoneNumber;

  private Long authUserId;

  @Column(nullable = false)
  private long tokenVersion = 1L;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "customer_id")
  private List<AddressEntity> addresses = new ArrayList<>();

  @Column(nullable = false)
  private String status = "ACTIVE";

  @Column(nullable = false)
  private String loyaltyTier = "BRONZE";

  @Column(nullable = false)
  private int loyaltyPoints = 0;

  private LocalDate dateOfBirth;

  private String gender;

  private String normalizedEmail;

  private String normalizedPhone;

  private String normalizedName;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public Long getAuthUserId() {
    return authUserId;
  }

  public void setAuthUserId(Long authUserId) {
    this.authUserId = authUserId;
  }

  public long getTokenVersion() {
    return tokenVersion;
  }

  public void setTokenVersion(long tokenVersion) {
    this.tokenVersion = tokenVersion;
  }

  public List<AddressEntity> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<AddressEntity> addresses) {
    this.addresses = addresses != null ? addresses : new ArrayList<>();
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getLoyaltyTier() {
    return loyaltyTier;
  }

  public void setLoyaltyTier(String loyaltyTier) {
    this.loyaltyTier = loyaltyTier;
  }

  public int getLoyaltyPoints() {
    return loyaltyPoints;
  }

  public void setLoyaltyPoints(int loyaltyPoints) {
    this.loyaltyPoints = loyaltyPoints;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public String getNormalizedEmail() {
    return normalizedEmail;
  }

  public void setNormalizedEmail(String normalizedEmail) {
    this.normalizedEmail = normalizedEmail;
  }

  public String getNormalizedPhone() {
    return normalizedPhone;
  }

  public void setNormalizedPhone(String normalizedPhone) {
    this.normalizedPhone = normalizedPhone;
  }

  public String getNormalizedName() {
    return normalizedName;
  }

  public void setNormalizedName(String normalizedName) {
    this.normalizedName = normalizedName;
  }
}
