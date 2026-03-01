/**
 * Order domain: aggregates, events, ports, policies, services, exceptions. No Spring or JPA
 * annotations in this package (per clean architecture). Exposed for report and other modules.
 */
@org.springframework.modulith.NamedInterface("domain")
@org.springframework.lang.NonNullApi
package com.chamrong.iecommerce.order.domain;
