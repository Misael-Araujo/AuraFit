package com.aurafit;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class AuraFitModuleTests {

  @Test
  void modulesRespectTheirDeclaredDependencies() {
    assertThatCode(() -> ApplicationModules.of(AuraFitApplication.class).verify())
        .doesNotThrowAnyException();
  }
}
