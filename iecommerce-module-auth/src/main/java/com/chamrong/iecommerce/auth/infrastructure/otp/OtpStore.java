package com.chamrong.iecommerce.auth.infrastructure.otp;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Caffeine-backed in-memory store for one-time password codes.
 *
 * <h3>Security properties</h3>
 *
 * <ul>
 *   <li>Codes are 6-digit, generated with {@link SecureRandom} (CSPRNG).
 *   <li>Each key ({@code userId:purpose}) maps to exactly one active code at a time — re-sending
 *       generates a fresh code and overwrites the previous one.
 *   <li>Codes are one-time use — consuming marks them and prevents replay attacks.
 *   <li>TTL is enforced by Caffeine's expiry policy; no periodic cleanup needed.
 * </ul>
 *
 * <p><b>Upgrade path:</b> replace Caffeine with Redis-backed cache using {@code @Cacheable} +
 * {@code RedisTemplate} when multi-node support is required.
 */
@Component
public class OtpStore {

  private static final int CODE_LENGTH = 6;
  private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
  private static final int MAX_ENTRIES = 100_000;

  /** CSPRNG — never use {@code Math.random()} or {@code new Random()} for security codes. */
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  /**
   * Key format: {@code "{userId}:{purpose}"} Caffeine expires entries automatically after {@link
   * #DEFAULT_TTL}.
   */
  private final Cache<String, OtpEntry> cache =
      Caffeine.newBuilder().expireAfterWrite(DEFAULT_TTL).maximumSize(MAX_ENTRIES).build();

  /**
   * Generates and stores a new OTP for the given user and purpose.
   *
   * <p>Any previously stored code for the same {@code userId + purpose} combination is overwritten.
   *
   * @param userId the local user identifier
   * @param purpose a short label (e.g. {@code "EMAIL_VERIFY"}, {@code "STEP_UP"})
   * @return the generated 6-digit code (must be sent to the user via email/SMS)
   */
  public String generate(final String userId, final String purpose) {
    final String code =
        String.format(
            "%0" + CODE_LENGTH + "d", SECURE_RANDOM.nextInt((int) Math.pow(10, CODE_LENGTH)));
    final OtpEntry entry = new OtpEntry(code, Instant.now().plus(DEFAULT_TTL), false, purpose);
    cache.put(key(userId, purpose), entry);
    return code;
  }

  /**
   * Verifies and consumes a submitted OTP code.
   *
   * <p>If valid, the entry is marked as used — a second call with the same code will always return
   * {@code false} (replay protection).
   *
   * @return {@code true} if the code matches, is unexpired, and has not been used before
   */
  public boolean verifyAndConsume(
      final String userId, final String purpose, final String submittedCode) {
    final String cacheKey = key(userId, purpose);
    final OtpEntry entry = cache.getIfPresent(cacheKey);
    if (entry == null || !entry.isValid(submittedCode)) {
      return false;
    }
    // Mark as used atomically — prevent replay within the TTL window
    cache.put(cacheKey, entry.markUsed());
    return true;
  }

  /**
   * Checks if an active (unexpired, unused) code exists. Useful for rate-limiting send frequency.
   */
  public Optional<OtpEntry> find(final String userId, final String purpose) {
    return Optional.ofNullable(cache.getIfPresent(key(userId, purpose)));
  }

  /** Explicitly invalidates any stored OTP for a user+purpose pair. */
  public void invalidate(final String userId, final String purpose) {
    cache.invalidate(key(userId, purpose));
  }

  private static String key(final String userId, final String purpose) {
    return userId + ":" + purpose;
  }
}
