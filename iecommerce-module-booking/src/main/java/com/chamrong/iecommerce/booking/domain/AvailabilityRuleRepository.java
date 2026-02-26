package com.chamrong.iecommerce.booking.domain;

import java.time.DayOfWeek;
import java.util.List;

public interface AvailabilityRuleRepository {
  AvailabilityRule save(AvailabilityRule rule);

  List<AvailabilityRule> findByResourceProductId(Long resourceProductId);

  List<AvailabilityRule> findByResourceProductIdAndDayOfWeek(Long resourceProductId, DayOfWeek day);

  List<AvailabilityRule> findByStaffIdAndDayOfWeek(Long staffId, DayOfWeek day);

  void deleteById(Long id);
}
