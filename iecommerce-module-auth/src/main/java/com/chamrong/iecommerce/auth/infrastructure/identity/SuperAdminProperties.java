package com.chamrong.iecommerce.auth.infrastructure.identity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the initial super admin account.
 *
 * <p>The credentials defined here are used only for bootstrapping the first super admin user in the
 * identity provider. The password should be treated as a temporary secret; Keycloak will enforce a
 * password change on first login via the {@code UPDATE_PASSWORD} required action.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.super-admin")
public class SuperAdminProperties {

  /**
   * Initial username for the platform super admin.
   *
   * <p>If not provided, a sensible default such as {@code admin} will be used by the initializer.
   */
  private String username;

  /**
   * Initial email for the platform super admin.
   *
   * <p>If not provided, the initializer will derive one from the username.
   */
  private String email;

  /**
   * Temporary bootstrap password for the super admin.
   *
   * <p>If omitted, the initializer will fall back to a default implementation-specific value. This
   * password is expected to be changed immediately on first login.
   */
  private String password;
}
