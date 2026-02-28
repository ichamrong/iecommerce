package com.chamrong.iecommerce.customer.domain.auth.port;

import com.chamrong.iecommerce.customer.domain.auth.AccountState;

public interface LoginAttemptPort {
  AccountState getAccountState(String customerId);

  void saveAccountState(AccountState state);
}
