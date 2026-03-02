package com.chamrong.iecommerce.sale.domain.ports;

/**
 * Port for writing tamper-evident audit logs for sale-related actions.
 *
 * <p>Implementations live in the infrastructure layer and may use JPA, external audit services, or
 * the dedicated audit module.
 */
public interface AuditPort {

  /**
   * Persist an audit record describing a business-relevant action.
   *
   * @param tenantId tenant identifier
   * @param actorId actor performing the action (staff id, system, etc.)
   * @param action symbolic action name (e.g. OPEN_SESSION, CONFIRM_QUOTATION)
   * @param entityName logical entity name (e.g. SaleSession, Quotation)
   * @param entityId entity identifier
   * @param correlationId correlation or trace id, if any
   * @param beforeState string representation of state before the action (may be masked/null)
   * @param afterState string representation of state after the action (may be masked/null)
   */
  void log(
      String tenantId,
      String actorId,
      String action,
      String entityName,
      String entityId,
      String correlationId,
      String beforeState,
      String afterState);
}
