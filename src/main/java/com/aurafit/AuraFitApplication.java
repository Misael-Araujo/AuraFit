package com.aurafit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableScheduling;

@Modulithic
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class AuraFitApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuraFitApplication.class, args);
  }
}
