package com.chamrong.iecommerce.audit.domain.model;

import java.util.Objects;

/**
 * Target of the audited action: type and identifier.
 *
 * @param targetType e.g. ORDER, PAYMENT, PRODUCT
 * @param targetId stable business or entity id
 */
public record AuditTarget(String targetType, String targetId) {

  public AuditTarget {
    Objects.requireNonNull(targetType, "targetType");
    targetId = targetId != null ? targetId : "";
  }
}
