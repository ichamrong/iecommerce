package com.chamrong.iecommerce.catalog.domain.service;

/**
 * Normalizes search input for consistent matching (trim, lower-case, avoid leading wildcards).
 *
 * <p>Used when building keyword filters for list/search. Prefer prefix matching for index use.
 */
public final class SearchNormalizationService {

  private SearchNormalizationService() {}

  /**
   * Normalizes a search keyword for use in queries.
   *
   * @param keyword raw input (may be null)
   * @return trimmed, lowercased string, or null if input was null/blank
   */
  public static String normalizeKeyword(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return null;
    }
    return keyword.trim().toLowerCase();
  }
}
