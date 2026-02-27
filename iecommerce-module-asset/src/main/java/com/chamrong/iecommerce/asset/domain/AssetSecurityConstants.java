package com.chamrong.iecommerce.asset.domain;

/**
 * Centralized security authority constants for the Asset module. Using these constants ensures
 * consistency across controllers and tests.
 */
public final class AssetSecurityConstants {

  private AssetSecurityConstants() {
    // Prevent instantiation
  }

  /** Authority required to read assets (search, view, download). */
  public static final String ASSET_READ = "assets:read";

  /** Authority required to manage assets (upload, move, rename, delete). */
  public static final String ASSET_MANAGE = "assets:manage";

  /** Authority required for administrative storage operations. */
  public static final String ASSET_ADMIN = "assets:admin";
}
