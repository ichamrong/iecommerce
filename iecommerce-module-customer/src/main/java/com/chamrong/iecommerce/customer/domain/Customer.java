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
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
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

  private java.time.LocalDate dateOfBirth;

  private String gender;

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
      addresses.forEach(a -> a.setDefaultBilling(false));
    }
    if (address.isDefaultShipping()) {
      addresses.forEach(a -> a.setDefaultShipping(false));
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
