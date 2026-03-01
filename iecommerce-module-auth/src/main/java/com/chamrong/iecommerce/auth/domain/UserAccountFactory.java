package com.chamrong.iecommerce.auth.domain;

import org.springframework.lang.NonNull;

/**
 * Static factory for constructing {@link User} entities.
 *
 * <p>Centralizes all {@code User} construction logic, eliminating the repeated {@code new User();
 * user.set*(...)} boilerplate scattered across handlers.
 *
 * <p>Following the <strong>Factory Method</strong> pattern: each named factory method encodes a
 * distinct creation scenario with its own invariants, defaults, and initial state.
 */
public final class UserAccountFactory {

  private UserAccountFactory() {
    throw new UnsupportedOperationException("Utility class — do not instantiate");
  }

  /**
   * Creates a user who registered themselves.
   *
   * <p>The account is immediately {@link UserAccountState#ACTIVE} — no admin approval required.
   *
   * @param username human-readable login name
   * @param email verified email address
   * @param tenantId owning tenant
   * @return a fully initialised (but unsaved) User entity
   */
  public static @NonNull User createSelfRegistered(
      @NonNull final String username, @NonNull final String email, @NonNull final String tenantId) {

    final User user = new User(tenantId, username.trim().toLowerCase(), email.trim().toLowerCase());
    user.activate();
    return user;
  }

  /**
   * Creates a user invited by an administrator.
   *
   * <p>The account starts as {@link UserAccountState#PENDING} — the user must complete first-login
   * password reset before the account transitions to {@link UserAccountState#ACTIVE}.
   *
   * @param username human-readable login name
   * @param email unverified email (verified during first-login flow)
   * @param tenantId owning tenant
   * @return a fully initialised (but unsaved) User entity
   */
  public static @NonNull User createAdminInvited(
      @NonNull final String username, @NonNull final String email, @NonNull final String tenantId) {

    final User user = new User(tenantId, username.trim().toLowerCase(), email.trim().toLowerCase());
    user.pendingActivation();
    return user;
  }

  /**
   * Creates a user authenticated via a social identity provider (Google, GitHub, etc.).
   *
   * <p>No password or temporary credential is involved. The account is immediately {@link
   * UserAccountState#ACTIVE} because Keycloak has verified the external identity.
   *
   * @param username derived from IDP (email prefix or preferred_username claim)
   * @param email verified by the external IDP
   * @param tenantId owning tenant
   * @return a fully initialised (but unsaved) User entity
   */
  public static @NonNull User createFromSocialLogin(
      @NonNull final String username, @NonNull final String email, @NonNull final String tenantId) {

    final User user = new User(tenantId, username.trim().toLowerCase(), email.trim().toLowerCase());
    user.activate();
    return user;
  }
}
