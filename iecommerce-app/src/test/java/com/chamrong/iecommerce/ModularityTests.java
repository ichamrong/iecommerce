package com.chamrong.iecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

  @Test
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
