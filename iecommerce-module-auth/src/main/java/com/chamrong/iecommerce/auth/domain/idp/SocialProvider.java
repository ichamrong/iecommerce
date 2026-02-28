package com.chamrong.iecommerce.auth.domain.idp;

/**
 * Descriptor for an external identity provider enabled in the Keycloak realm.
 *
 * <p>This is a read-only value object returned by {@link
 * com.chamrong.iecommerce.auth.domain.IdentityService#listSocialProviders()}.
 *
 * @param type Strongly-typed provider category.
 * @param alias Keycloak's {@code idpHint} value — used in the frontend redirect URL.
 * @param displayName Human-readable label shown in the login UI.
 * @param enabled Whether this provider is active in the realm.
 */
public record SocialProvider(
    IdentityProviderType type, String alias, String displayName, boolean enabled) {}
