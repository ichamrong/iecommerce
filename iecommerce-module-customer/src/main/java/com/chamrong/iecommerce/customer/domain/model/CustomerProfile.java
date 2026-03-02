package com.chamrong.iecommerce.customer.domain.model;

import java.time.LocalDate;

/**
 * Value object for customer profile data (name, phone, email, optional DOB). Used for display and
 * updates.
 */
public final class CustomerProfile {

  private final String firstName;
  private final String lastName;
  private final String phoneNumber;
  private final String email;
  private final LocalDate dateOfBirth;
  private final String gender;

  public CustomerProfile(
      String firstName,
      String lastName,
      String phoneNumber,
      String email,
      LocalDate dateOfBirth,
      String gender) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.dateOfBirth = dateOfBirth;
    this.gender = gender;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getEmail() {
    return email;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public String getGender() {
    return gender;
  }
}
