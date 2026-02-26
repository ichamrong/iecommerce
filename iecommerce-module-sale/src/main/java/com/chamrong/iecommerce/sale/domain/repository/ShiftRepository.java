package com.chamrong.iecommerce.sale.domain.repository;

import com.chamrong.iecommerce.sale.domain.Shift;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
  Optional<Shift> findByStaffIdAndStatus(String staffId, Shift.ShiftStatus status);
}
