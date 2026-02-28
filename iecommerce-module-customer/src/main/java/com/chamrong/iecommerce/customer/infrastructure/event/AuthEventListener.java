package com.chamrong.iecommerce.customer.infrastructure.event;

import com.chamrong.iecommerce.auth.domain.event.UserRegisteredEvent;
import com.chamrong.iecommerce.customer.application.command.CreateCustomerCommand;
import com.chamrong.iecommerce.customer.application.command.CreateCustomerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class AuthEventListener {

  private static final Logger log = LoggerFactory.getLogger(AuthEventListener.class);
  private final CreateCustomerHandler createCustomerHandler;

  public AuthEventListener(CreateCustomerHandler createCustomerHandler) {
    this.createCustomerHandler = createCustomerHandler;
  }

  @ApplicationModuleListener
  public void onUserRegistered(UserRegisteredEvent event) {
    log.info(
        "Received UserRegisteredEvent: creating customer profile for user id {}", event.userId());

    // We leave firstName and lastName null as per the updated entity, allowing the user to update
    // them later.
    CreateCustomerCommand command =
        new CreateCustomerCommand(
            null, null, event.email(), null, event.userId(), event.tenantId());

    createCustomerHandler.handle(command);

    log.info("Customer profile created successfully for auth user id {}", event.userId());
  }
}
