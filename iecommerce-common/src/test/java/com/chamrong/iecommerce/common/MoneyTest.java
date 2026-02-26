package com.chamrong.iecommerce.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void shouldRoundToBankersStandard() {
    // HALF_EVEN rounding (Banker's Rounding) rounds to nearest even neighbor when equidistant
    Money moneyEquidistantUp =
        Money.of("1.425", "USD"); // halfway between 1.42 and 1.43 -> rounds to 1.42 (even)
    Money moneyEquidistantDown =
        Money.of("1.415", "USD"); // halfway between 1.41 and 1.42 -> rounds to 1.42 (even)

    // BUT our scale is 4, so let's test at scale 4:
    Money money4Up =
        Money.of("1.42505", "USD"); // halfway between 1.4250 and 1.4251 -> rounds to 1.4250
    Money money4Down =
        Money.of("1.41505", "USD"); // halfway between 1.4150 and 1.4151 -> rounds to 1.4150

    assertEquals(new BigDecimal("1.4250"), money4Up.getAmount());
    assertEquals(new BigDecimal("1.4150"), money4Down.getAmount());
  }

  @Test
  void testAddExactDecimals() {
    Money m1 = Money.of("1.41", "USD");
    Money m2 = Money.of("1.42", "USD");

    Money result = m1.add(m2);

    // Should be exactly 2.83 at scale 4
    assertEquals(new BigDecimal("2.8300"), result.getAmount());
    assertEquals("USD", result.getCurrency());
  }

  @Test
  void cannotAddDifferentCurrencies() {
    Money usd = Money.of("10.00", "USD");
    Money eur = Money.of("10.00", "EUR");

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> usd.add(eur));
    assertEquals("Currency mismatch: cannot operate on USD and EUR", ex.getMessage());
  }

  @Test
  void zeroFloorOnSubtract() {
    Money ten = Money.of("10.00", "USD");
    Money fifteen = Money.of("15.00", "USD");

    // Financial invariant: cannot have negative money
    Money result = ten.subtract(fifteen);
    assertEquals(new BigDecimal("0.0000"), result.getAmount());
  }
}
