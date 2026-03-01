package com.chamrong.iecommerce.customer.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    indexes = {@Index(name = "idx_customer_cursor", columnList = "created_at DESC, id DESC")})
public class Customer extends BaseTenantEntity {

  @Column(nullable = true)
  private String firstName;

  @Column(nullable = true)
  private String lastName;

  @Column(unique = true, nullable = false)
  private String email;

  private String phoneNumber;

  @Column(name = "auth_user_id")
  private Long authUserId; // Link to Auth module's User

  @Column(name = "token_version", nullable = false)
  private long tokenVersion = 1L;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "customer_id")
  private List<Address> addresses = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CustomerStatus status = CustomerStatus.ACTIVE;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LoyaltyTier loyaltyTier = LoyaltyTier.BRONZE;

  @Column(nullable = false)
  private int loyaltyPoints = 0;

  private LocalDate dateOfBirth;

  private String gender;

  public Customer() {}

  public Customer(String tenantId, String email) {
    setTenantId(tenantId);
    this.email = email;
  }

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

  public List<Address> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<Address> addresses) {
    this.addresses = addresses;
  }

  public CustomerStatus getStatus() {
    return status;
  }

  public void setStatus(CustomerStatus status) {
    this.status = status;
  }

  public LoyaltyTier getLoyaltyTier() {
    return loyaltyTier;
  }

  public void setLoyaltyTier(LoyaltyTier loyaltyTier) {
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

  // ── Profile mutations ────────────────────────────────────────────────────

  public void updateProfile(String firstName, String lastName, String phoneNumber) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
  }

  public void linkAuthUser(Long authUserId) {
    this.authUserId = authUserId;
  }

  // ── Domain Methods ────────────────────────────────────────────────────────

  public void block() {
    this.status = CustomerStatus.BLOCKED;
  }

  public void unblock() {
    this.status = CustomerStatus.ACTIVE;
  }

  public void addPoints(int points) {
    if (points < 0) throw new IllegalArgumentException("Points cannot be negative");
    this.loyaltyPoints += points;
    updateTier();
  }

  private void updateTier() {
    if (this.loyaltyPoints >= 10000) {
      this.loyaltyTier = LoyaltyTier.PLATINUM;
    } else if (this.loyaltyPoints >= 5000) {
      this.loyaltyTier = LoyaltyTier.GOLD;
    } else if (this.loyaltyPoints >= 1000) {
      this.loyaltyTier = LoyaltyTier.SILVER;
    } else {
      this.loyaltyTier = LoyaltyTier.BRONZE;
    }
  }

  public void addAddress(Address address) {
    if (address.isDefaultBilling()) {
      addresses.forEach(Address::clearDefaultBilling);
    }
    if (address.isDefaultShipping()) {
      addresses.forEach(Address::clearDefaultShipping);
    }
    this.addresses.add(address);
  }

  public void removeAddress(Address address) {
    this.addresses.remove(address);
  }

  public void incrementTokenVersion() {
    this.tokenVersion++;
  }
}
