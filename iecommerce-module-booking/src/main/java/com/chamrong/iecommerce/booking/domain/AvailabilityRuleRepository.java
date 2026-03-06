package com.chamrong.iecommerce.booking.domain;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface AvailabilityRuleRepository {
  AvailabilityRule save(AvailabilityRule rule);

  Optional<AvailabilityRule> findById(Long id);

  List<AvailabilityRule> findByResourceProductId(Long resourceProductId);

  List<AvailabilityRule> findByResourceProductIdAndDayOfWeek(Long resourceProductId, DayOfWeek day);

  List<AvailabilityRule> findByStaffIdAndDayOfWeek(Long staffId, DayOfWeek day);

  void delete(AvailabilityRule rule);

  void deleteById(Long id);
}
