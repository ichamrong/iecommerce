package com.chamrong.iecommerce.customer.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.customer.domain.model.Address;
import com.chamrong.iecommerce.customer.domain.model.Customer;
import com.chamrong.iecommerce.customer.domain.model.CustomerStatus;
import com.chamrong.iecommerce.customer.domain.model.LoyaltyTier;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Maps between JPA entities and domain models. */
@Component
public class EntityMappers {

  public Customer toDomain(CustomerEntity e) {
    if (e == null) return null;
    Customer c = new Customer();
    c.setId(e.getId());
    c.setTenantId(e.getTenantId());
    c.setCreatedAt(e.getCreatedAt());
    c.setUpdatedAt(e.getUpdatedAt());
    c.setFirstName(e.getFirstName());
    c.setLastName(e.getLastName());
    c.setEmail(e.getEmail());
    c.setPhoneNumber(e.getPhoneNumber());
    c.setAuthUserId(e.getAuthUserId());
    c.setTokenVersion(e.getTokenVersion());
    c.setStatus(statusFrom(e.getStatus()));
    c.setLoyaltyTier(loyaltyTierFrom(e.getLoyaltyTier()));
    c.setLoyaltyPoints(e.getLoyaltyPoints());
    c.setDateOfBirth(e.getDateOfBirth());
    c.setGender(e.getGender());
    c.setNormalizedEmail(e.getNormalizedEmail());
    c.setNormalizedPhone(e.getNormalizedPhone());
    c.setNormalizedName(e.getNormalizedName());
    if (e.getAddresses() != null) {
      e.getAddresses().forEach(a -> c.getAddresses().add(toDomainAddress(a)));
    }
    return c;
  }

  public Address toDomain(AddressEntity e) {
    return toDomainAddress(e);
  }

  private static Address toDomainAddress(AddressEntity e) {
    if (e == null) return null;
    Address a = new Address();
    a.setId(e.getId());
    a.setStreet(e.getStreet());
    a.setCity(e.getCity());
    a.setState(e.getState());
    a.setCountry(e.getCountry());
    a.setPostalCode(e.getPostalCode());
    a.setDefaultShipping(e.isDefaultShipping());
    a.setDefaultBilling(e.isDefaultBilling());
    return a;
  }

  public CustomerEntity toEntity(Customer c) {
    if (c == null) return null;
    CustomerEntity e = new CustomerEntity();
    e.setId(c.getId());
    e.setTenantId(c.getTenantId());
    e.setFirstName(c.getFirstName());
    e.setLastName(c.getLastName());
    e.setEmail(c.getEmail());
    e.setPhoneNumber(c.getPhoneNumber());
    e.setAuthUserId(c.getAuthUserId());
    e.setTokenVersion(c.getTokenVersion());
    e.setStatus(c.getStatus() != null ? c.getStatus().name() : "ACTIVE");
    e.setLoyaltyTier(c.getLoyaltyTier() != null ? c.getLoyaltyTier().name() : "BRONZE");
    e.setLoyaltyPoints(c.getLoyaltyPoints());
    e.setDateOfBirth(c.getDateOfBirth());
    e.setGender(c.getGender());
    e.setNormalizedEmail(c.getNormalizedEmail());
    e.setNormalizedPhone(c.getNormalizedPhone());
    e.setNormalizedName(c.getNormalizedName());
    if (c.getAddresses() != null) {
      List<AddressEntity> list = new ArrayList<>();
      for (Address a : c.getAddresses()) {
        AddressEntity ae = toEntityAddress(a);
        ae.setCustomer(e);
        list.add(ae);
      }
      e.setAddresses(list);
    }
    return e;
  }

  public AddressEntity toEntity(Address a, CustomerEntity customer) {
    AddressEntity e = toEntityAddress(a);
    if (e != null && customer != null) e.setCustomer(customer);
    return e;
  }

  private static AddressEntity toEntityAddress(Address a) {
    if (a == null) return null;
    AddressEntity e = new AddressEntity();
    e.setId(a.getId());
    e.setStreet(a.getStreet());
    e.setCity(a.getCity());
    e.setState(a.getState());
    e.setCountry(a.getCountry());
    e.setPostalCode(a.getPostalCode());
    e.setDefaultShipping(a.isDefaultShipping());
    e.setDefaultBilling(a.isDefaultBilling());
    return e;
  }

  private static CustomerStatus statusFrom(String s) {
    if (s == null) return CustomerStatus.ACTIVE;
    try {
      return CustomerStatus.valueOf(s);
    } catch (Exception e) {
      return CustomerStatus.ACTIVE;
    }
  }

  private static LoyaltyTier loyaltyTierFrom(String s) {
    if (s == null) return LoyaltyTier.BRONZE;
    try {
      return LoyaltyTier.valueOf(s);
    } catch (Exception e) {
      return LoyaltyTier.BRONZE;
    }
  }
}
