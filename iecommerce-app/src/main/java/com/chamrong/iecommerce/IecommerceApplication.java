package com.chamrong.iecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
    exclude = {
      org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class
    })
@EntityScan(basePackages = "com.chamrong.iecommerce")
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.chamrong.iecommerce", considerNestedRepositories = true)
@EnableRedisRepositories(basePackages = "com.chamrong.iecommerce.redis")
@EnableScheduling
public class IecommerceApplication {

  public static void main(String[] args) {
    SpringApplication.run(IecommerceApplication.class, args);
  }
}
