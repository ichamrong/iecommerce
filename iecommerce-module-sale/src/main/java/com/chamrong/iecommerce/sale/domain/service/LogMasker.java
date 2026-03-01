package com.chamrong.iecommerce.sale.domain.service;

import java.util.regex.Pattern;

/**
 * BG10: Privacy-aware structured logging (Masking). Redacts PII from string representations of
 * domain objects.
 */
public class LogMasker {

  private static final Pattern CARD_PATTERN = Pattern.compile("\\b(?:\\d[ -]?){13,16}\\b");
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
  private static final Pattern PHONE_PATTERN =
      Pattern.compile("\\b(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b");

  public static String mask(Object obj) {
    if (obj == null) return "null";
    String text = obj.toString();

    text = CARD_PATTERN.matcher(text).replaceAll("****-****-****-****");
    text = EMAIL_PATTERN.matcher(text).replaceAll("****@****.***");
    text = PHONE_PATTERN.matcher(text).replaceAll("********");

    return text;
  }
}
