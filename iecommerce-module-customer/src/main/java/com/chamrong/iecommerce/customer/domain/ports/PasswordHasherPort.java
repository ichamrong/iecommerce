package com.chamrong.iecommerce.customer.domain.ports;

/** Port for password hashing and verification (e.g. BCrypt/Argon2). */
public interface PasswordHasherPort {

  String hash(String rawPassword);

  boolean verify(String rawPassword, String hashedPassword);
}
