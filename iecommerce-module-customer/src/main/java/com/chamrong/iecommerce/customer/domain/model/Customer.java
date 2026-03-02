package com.chamrong.iecommerce.customer.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer aggregate root. Pure domain model; no Spring or JPA. Supports e-commerce, POS, and
 * accommodation guest profiles.
 */
public class Customer {

  private Long id;
  private String tenantId;
  private Instant createdAt;
  private Instant updatedAt;

  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private Long authUserId;
  private long tokenVersion = 1L;

  private final List<Address> addresses = new ArrayList<>();
  private CustomerStatus status = CustomerStatus.ACTIVE;
  private LoyaltyTier loyaltyTier = LoyaltyTier.BRONZE;
  private int loyaltyPoints = 0;
  private LocalDate dateOfBirth;
  private String gender;

  /** Normalized fields for search (lowercase/trim); set by infrastructure or domain service. */
  private String normalizedEmail;

  private String normalizedPhone;
  private String normalizedName;

  public Customer() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
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

  // ── Domain behaviour ─────────────────────────────────────────────────────

  public void updateProfile(String firstName, String lastName, String phoneNumber) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
  }

  public void linkAuthUser(Long authUserId) {
    this.authUserId = authUserId;
  }

  public void block() {
    this.status = CustomerStatus.BLOCKED;
  }

  public void unblock() {
    this.status = CustomerStatus.ACTIVE;
  }

  public void lock() {
    this.status = CustomerStatus.LOCKED;
  }

  public void unlock() {
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
