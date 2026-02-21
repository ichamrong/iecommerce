package com.chamrong.iecommerce.auth.application.query;

import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.UserRepository;
import com.chamrong.iecommerce.common.TenantContext;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class UserQueryHandler {

  private final UserRepository userRepository;

  public UserQueryHandler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Page<User> findAllUsers(Pageable pageable) {
    // Hard limit on page size to prevent performance issues
    if (pageable.getPageSize() > 100) {
      pageable = PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort());
    }

    // Enforce tenant isolation on the "list all" operation
    String currentTenant = TenantContext.getCurrentTenant();
    if (currentTenant == null) {
      return Page.empty(pageable);
    }
    return userRepository.findByTenantId(currentTenant, pageable);
  }

  public Optional<User> findUserById(Long id) {
    return userRepository.findById(id);
  }
}
