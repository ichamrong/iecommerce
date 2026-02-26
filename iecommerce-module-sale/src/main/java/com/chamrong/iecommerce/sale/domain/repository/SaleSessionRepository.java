package com.chamrong.iecommerce.sale.domain.repository;

import com.chamrong.iecommerce.sale.domain.SaleSession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleSessionRepository extends JpaRepository<SaleSession, Long> {
  List<SaleSession> findByShiftIdAndStatus(Long shiftId, SaleSession.SessionStatus status);
}
