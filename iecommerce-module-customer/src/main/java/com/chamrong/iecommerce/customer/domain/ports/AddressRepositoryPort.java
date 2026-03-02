package com.chamrong.iecommerce.customer.domain.ports;

import com.chamrong.iecommerce.customer.domain.model.Address;
import java.util.List;
import java.util.Optional;

/** Port for customer address persistence. Addresses belong to a customer (tenant-scoped). */
public interface AddressRepositoryPort {

  Address save(Address address);

  Optional<Address> findById(Long id);

  List<Address> findByCustomerId(Long customerId);

  void delete(Address address);
}
