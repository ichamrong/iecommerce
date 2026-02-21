package com.chamrong.iecommerce.staff.application.query;

import com.chamrong.iecommerce.staff.application.dto.StaffResponse;
import com.chamrong.iecommerce.staff.domain.StaffProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StaffQueryHandler {

  private final StaffProfileRepository staffProfileRepository;
  private final com.chamrong.iecommerce.staff.application.StaffMapper mapper;

  public StaffQueryHandler(
      StaffProfileRepository staffProfileRepository,
      com.chamrong.iecommerce.staff.application.StaffMapper mapper) {
    this.staffProfileRepository = staffProfileRepository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public Page<StaffResponse> findAll(Pageable pageable) {
    return staffProfileRepository.findAll(pageable).map(mapper::toResponse);
  }

  @Transactional(readOnly = true)
  public StaffResponse findById(Long id) {
    return staffProfileRepository
        .findById(id)
        .map(mapper::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + id));
  }
}
