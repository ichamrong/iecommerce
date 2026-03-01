/**
 * Catalog domain events — published when items are created, updated, published, or archived.
 *
 * <p>Consumers (inventory, order, sale, booking) may subscribe via outbox or message bus.
 */
@org.springframework.lang.NonNullApi
package com.chamrong.iecommerce.catalog.domain.event;
