package com.chamrong.iecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.chamrong.iecommerce")
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.chamrong.iecommerce")
@EnableScheduling
public class IecommerceApplication {

  public static void main(String[] args) {
    SpringApplication.run(IecommerceApplication.class, args);
  }
}
