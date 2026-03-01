/**
 * Repository and service ports for the payment domain, exposed as a named interface so the report
 * module (and other cross-cutting modules) can depend on {@code PaymentIntentRepositoryPort}
 * without violating Spring Modulith boundaries.
 */
@org.springframework.modulith.NamedInterface("domain.ports")
@org.springframework.lang.NonNullApi
package com.chamrong.iecommerce.payment.domain.ports;
