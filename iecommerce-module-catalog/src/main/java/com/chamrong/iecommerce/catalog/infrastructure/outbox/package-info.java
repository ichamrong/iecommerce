/**
 * Catalog outbox — reliable event publishing for catalog mutations.
 *
 * <p>Events (created, updated, published, archived) are written to outbox table and relayed
 * asynchronously to avoid losing events on failure.
 */
@org.springframework.lang.NonNullApi
package com.chamrong.iecommerce.catalog.infrastructure.outbox;
