package com.chamrong.iecommerce.catalog.api;

/**
 * Standard error response for all Catalog API endpoints.
 *
 * @param message human-readable error description
 * @param code machine-readable error code (e.g. {@code CATALOG_NOT_FOUND})
 */
public record CatalogErrorResponse(String message, String code) {}
