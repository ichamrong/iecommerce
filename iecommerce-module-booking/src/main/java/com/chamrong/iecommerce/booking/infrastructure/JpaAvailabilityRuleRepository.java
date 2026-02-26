package com.chamrong.iecommerce.booking.infrastructure;

import com.chamrong.iecommerce.booking.domain.AvailabilityRule;
import com.chamrong.iecommerce.booking.domain.AvailabilityRuleRepository;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link AvailabilityRuleRepository} port. */
@Repository
public interface JpaAvailabilityRuleRepository
    extends JpaRepository<AvailabilityRule, Long>, AvailabilityRuleRepository {

  @Override
  List<AvailabilityRule> findByResourceProductId(Long resourceProductId);

  @Override
  List<AvailabilityRule> findByResourceProductIdAndDayOfWeek(
      Long resourceProductId, DayOfWeek dayOfWeek);

  @Override
  List<AvailabilityRule> findByStaffIdAndDayOfWeek(Long staffId, DayOfWeek dayOfWeek);
}
