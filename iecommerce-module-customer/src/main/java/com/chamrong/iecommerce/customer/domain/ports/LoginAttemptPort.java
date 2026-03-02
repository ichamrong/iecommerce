package com.chamrong.iecommerce.customer.domain.ports;

import com.chamrong.iecommerce.customer.domain.model.LoginAttempt;

/**
 * Port for login attempt / lockout state (e.g. Redis). Tracks consecutive failures and lock until.
 */
public interface LoginAttemptPort {

  LoginAttempt getAccountState(String customerId);

  void saveAccountState(LoginAttempt state);
}
