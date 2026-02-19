package com.chamrong.iecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class IecommerceApplication {

  public static void main(String[] args) {
    SpringApplication.run(IecommerceApplication.class, args);
  }
}
