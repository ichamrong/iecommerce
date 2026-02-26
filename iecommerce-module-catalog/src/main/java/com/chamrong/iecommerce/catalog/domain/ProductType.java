package com.chamrong.iecommerce.catalog.domain;

/** Classification of the product kind, affecting fulfillment and stock tracking. */
public enum ProductType {

  /** Has physical stock, weight, dimensions. Managed by the inventory module. */
  PHYSICAL,

  /** Downloadable file or license key. No physical stock tracking. */
  DIGITAL,

  /** Intangible service (e.g., massage, consultation). No stock tracking. */
  SERVICE,

  /** Time-based service (appointment). Managed by booking module. */
  BOOKING,

  /** Multi-night stay (hotel, rental). Managed by booking/order modules. */
  ACCOMMODATION
}
