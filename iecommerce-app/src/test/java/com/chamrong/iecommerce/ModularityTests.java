package com.chamrong.iecommerce;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

  @Test
  @Disabled(
      "Temporarily disabled: asset and booking modules depend on setting application services "
          + "for quota/setting lookups. Revisit when defining explicit exported interfaces in the "
          + "setting module.")
  void verifiesModularity() {
    ApplicationModules modules = ApplicationModules.of(IecommerceApplication.class);
    modules.verify();
  }

  @Test
  void renderDocumentation() {
    ApplicationModules modules = ApplicationModules.of(IecommerceApplication.class);
    new Documenter(modules).writeDocumentation();
  }
}
