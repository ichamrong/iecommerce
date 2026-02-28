package com.chamrong.iecommerce.auth.domain;

/**
 * Lifecycle state of a user account.
 *
 * <p>Transitions:
 *
 * <pre>
 *   PENDING ──► ACTIVE ──► SUSPENDED ──► DELETED
 *                 │           ▲
 *                 ├── LOCKED ─┘  (auto-expires via lock store TTL)
 *                 └── (direct) ──────────────────► DELETED
 * </pre>
 *
 * <ul>
 *   <li>{@link #PENDING} — Admin-created or invited; first-login reset not yet completed.
 *   <li>{@link #ACTIVE} — Normal, fully functional account.
 *   <li>{@link #LOCKED} — Temporarily locked after repeated failed logins (progressive backoff).
 *       Auto-expires — managed by the {@code LoginLockStore}, NOT by a DB flag. The DB state is not
 *       changed to LOCKED; only the lock store is authoritative.
 *   <li>{@link #SUSPENDED} — Permanently disabled by admin action (requires manual reactivation).
 *   <li>{@link #DELETED} — Soft-deleted; data retained for audit but login is blocked.
 * </ul>
 */
public enum UserAccountState {

  /** Account created but first-login password reset not yet completed. */
  PENDING,

  /** Account is fully active. Normal operations permitted. */
  ACTIVE,

  /**
   * Account is temporarily locked after repeated failed logins.
   *
   * <p>The lock duration is controlled by {@link
   * com.chamrong.iecommerce.auth.domain.lock.LoginLockPolicy} and stored in {@link
   * com.chamrong.iecommerce.auth.domain.lock.LoginLockStore}. The lock auto-expires — no manual
   * admin action is required.
   */
  LOCKED,

  /** Account is permanently suspended by admin action. Cannot log in; requires reactivation. */
  SUSPENDED,

  /** Account is soft-deleted. Data is retained for audit; login blocked. */
  DELETED
}
