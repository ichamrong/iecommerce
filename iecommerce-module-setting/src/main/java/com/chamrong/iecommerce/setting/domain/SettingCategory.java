package com.chamrong.iecommerce.setting.domain;

/** Logical grouping for settings to support filtered retrieval. */
public enum SettingCategory {
  /** General store information (name, timezone, currency, locale). */
  GENERAL,

  /** Email / SMTP integration credentials. */
  EMAIL,

  /** SMS gateway credentials (Twilio, Vonage). */
  SMS,

  /** WhatsApp Business API credentials. */
  WHATSAPP,

  /** Telegram bot integration. */
  TELEGRAM,

  /** Push notification services (FCM, OneSignal). */
  PUSH_NOTIFICATION,

  /** Numeric quota limits enforced per tenant subscription plan. */
  QUOTA,

  /** Security-related configuration (session timeouts, 2FA). */
  SECURITY,

  /** Payment gateway credentials (Stripe, PayPal). */
  PAYMENT,

  /** Shipping provider configuration. */
  SHIPPING,

  /** UI / storefront customisation (theme, fonts, colours). */
  APPEARANCE,

  /** Feature flags for toggling system functionalities dynamically. */
  FEATURE_FLAG,
}
