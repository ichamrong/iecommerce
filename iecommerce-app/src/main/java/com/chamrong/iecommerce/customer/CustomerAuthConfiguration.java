package com.chamrong.iecommerce.customer;

import com.chamrong.iecommerce.customer.domain.auth.DefaultLoginLockPolicy;
import com.chamrong.iecommerce.customer.domain.auth.LoginLockPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application-level configuration for customer authentication components.
 *
 * <p>Wires the {@link LoginLockPolicy} used by {@code LoginCustomerHandler}. For now this uses the
 * simple {@link DefaultLoginLockPolicy} implementation suitable for local development.
 */
@Configuration
public class CustomerAuthConfiguration {

  @Bean
  public LoginLockPolicy customerLoginLockPolicy() {
    return new DefaultLoginLockPolicy();
  }
}
