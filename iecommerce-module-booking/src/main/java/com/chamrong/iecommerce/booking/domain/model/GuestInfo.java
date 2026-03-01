package com.chamrong.iecommerce.booking.domain.model;

import java.util.Objects;

/**
 * Guest details (PII minimized). Store only what is required for check-in verification.
 *
 * @param firstName optional
 * @param lastName  optional
 * @param email     optional; validate format if used
 * @param phone     optional
 */
public record GuestInfo(String firstName, String lastName, String email, String phone) {

  public GuestInfo {
    firstName = firstName != null ? firstName.trim() : "";
    lastName = lastName != null ? lastName.trim() : "";
    email = email != null ? email.trim() : "";
    phone = phone != null ? phone.trim() : "";
  }

  public static GuestInfo empty() {
    return new GuestInfo("", "", "", "");
  }

  public boolean isEmpty() {
    return (firstName == null || firstName.isEmpty())
        && (lastName == null || lastName.isEmpty())
        && (email == null || email.isEmpty())
        && (phone == null || phone.isEmpty());
  }
}
