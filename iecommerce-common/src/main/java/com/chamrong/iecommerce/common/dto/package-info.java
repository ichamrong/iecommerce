/**
 * Shared pagination and response DTOs exposed as a named interface so other modules (promotion,
 * invoice, catalog, etc.) can use {@code CursorPage} and related types without violating Spring
 * Modulith boundaries.
 */
@org.springframework.modulith.NamedInterface("dto")
@org.springframework.lang.NonNullApi
package com.chamrong.iecommerce.common.dto;
