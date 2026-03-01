package com.chamrong.iecommerce.promotion.domain.rule.dsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Utility for parsing and validating rule definitions. */
@Component
public class RuleParser {
  private static final Logger log = LoggerFactory.getLogger(RuleParser.class);
  private final ObjectMapper objectMapper;

  public RuleParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public RuleDefinition parse(String json) {
    try {
      if (json == null || json.isBlank()) return null;
      return objectMapper.readValue(json, RuleDefinition.class);
    } catch (Exception e) {
      log.error("Failed to parse promotion rule JSON: {}", json, e);
      return null;
    }
  }

  public String toJson(RuleDefinition definition) {
    try {
      return objectMapper.writeValueAsString(definition);
    } catch (Exception e) {
      log.error("Failed to serialize promotion rule to JSON", e);
      return null;
    }
  }
}
