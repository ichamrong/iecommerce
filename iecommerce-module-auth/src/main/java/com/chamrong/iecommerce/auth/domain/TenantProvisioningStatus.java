package com.chamrong.iecommerce.auth.domain;

/** Tracks the state of the distributed tenant provisioning operation (Saga state). */
public enum TenantProvisioningStatus {
  /** Initial state, local DB entry created. */
  INITIAL,

  /** IDP (Keycloak) user and realm configurations created. */
  IDP_CREATED,

  /** Local roles and permissions synchronized. */
  LOCAL_SYNCED,

  /** Provisioning fully successful. */
  COMPLETED,

  /** Provisioning failed; requires manual or automated compensation. */
  FAILED
}
