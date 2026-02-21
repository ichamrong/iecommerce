package com.chamrong.iecommerce.staff.application.query;

import com.chamrong.iecommerce.staff.application.command.CreateStaffHandler;
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

  public StaffQueryHandler(StaffProfileRepository staffProfileRepository) {
    this.staffProfileRepository = staffProfileRepository;
  }

  @Transactional(readOnly = true)
  public Page<StaffResponse> findAll(Pageable pageable) {
    return staffProfileRepository.findAll(pageable).map(CreateStaffHandler::toResponse);
  }

  @Transactional(readOnly = true)
  public StaffResponse findById(Long id) {
    return staffProfileRepository
        .findById(id)
        .map(CreateStaffHandler::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + id));
  }
}
