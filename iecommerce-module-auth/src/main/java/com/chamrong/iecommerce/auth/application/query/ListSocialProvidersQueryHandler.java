package com.chamrong.iecommerce.auth.application.query;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.idp.SocialProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Retrieves the list of active social / federated identity providers configured in the IDP. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListSocialProvidersQueryHandler {

  private final IdentityService identityService;

  public List<SocialProvider> handle() {
    log.debug("Fetching configured social providers");
    return identityService.listSocialProviders();
  }
}
