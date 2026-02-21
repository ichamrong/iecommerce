package com.chamrong.iecommerce.staff.application.command;

import com.chamrong.iecommerce.staff.StaffReactivatedEvent;
import com.chamrong.iecommerce.staff.domain.StaffProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReactivateStaffHandler {

  private final StaffProfileRepository staffProfileRepository;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(Long id) {
    var profile =
        staffProfileRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + id));
    profile.reactivate();
    staffProfileRepository.save(profile);
    eventPublisher.publishEvent(new StaffReactivatedEvent(null, id));
  }
}
