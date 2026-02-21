package com.chamrong.iecommerce.staff.domain;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StaffProfileRepository {
  Optional<StaffProfile> findByUserId(String userId);

  Optional<StaffProfile> findById(Long id);

  boolean existsByUserId(String userId);

  Page<StaffProfile> findAll(Pageable pageable);

  StaffProfile save(StaffProfile profile);
}
