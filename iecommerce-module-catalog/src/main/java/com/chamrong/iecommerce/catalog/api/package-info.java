/**
 * Catalog API layer — REST controllers and optional CatalogApi facade.
 *
 * <p>Exposes product, category, and (optionally) price-rule and accommodation endpoints. All
 * endpoints enforce tenant scope and use cursor pagination for list operations.
 */
@org.springframework.lang.NonNullApi
package com.chamrong.iecommerce.catalog.api;
