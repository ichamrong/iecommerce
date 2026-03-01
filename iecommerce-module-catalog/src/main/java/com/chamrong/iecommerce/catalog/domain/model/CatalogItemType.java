package com.chamrong.iecommerce.catalog.domain.model;

/**
 * Classification of a catalog item for multi-vertical support.
 *
 * <p>Determines fulfillment, stock, and booking behavior. Used for validation and feature gating
 * (e.g. ROOM_TYPE/ROOM_UNIT require Accommodation add-on).
 */
public enum CatalogItemType {

  /** Standard e-commerce product (SKUs, variants, inventory). */
  PRODUCT,

  /** POS item — fast lookup by barcode/SKU; compatible with sale module. */
  POS_ITEM,

  /** Add-on service (cleaning, minibar, tour). */
  SERVICE,

  /** Accommodation room type — rate plan link, capacity. */
  ROOM_TYPE,

  /** Physical room unit with unique code (e.g. Room 101). */
  ROOM_UNIT,

  /** Slot-based appointment service (future). */
  APPOINTMENT_SERVICE
}
