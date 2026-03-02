package com.chamrong.iecommerce.customer.domain.service;

/** Normalizes search inputs: lowercase, trim, no leading wildcard. For indexed prefix match. */
public final class CustomerSearchNormalizationService {

  private CustomerSearchNormalizationService() {}

  public static String normalizeEmail(String email) {
    if (email == null || email.isBlank()) return null;
    return email.trim().toLowerCase();
  }

  public static String normalizePhone(String phone) {
    if (phone == null || phone.isBlank()) return null;
    return phone.trim().replaceAll("\\s+", "");
  }

  public static String normalizeName(String name) {
    if (name == null || name.isBlank()) return null;
    return name.trim().toLowerCase();
  }

  /** For search query: prefix-only (no leading wildcard) for index use. */
  public static String toPrefixSearch(String search) {
    if (search == null || search.isBlank()) return null;
    String n = search.trim().toLowerCase();
    return n.isEmpty() ? null : n + "%";
  }
}
