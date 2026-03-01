package com.chamrong.iecommerce.sale.domain.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * In-memory Token Bucket implementation for rate limiting (BG8). Big-O: O(1) for check and consume.
 * Space: O(N) where N is number of unique keys.
 */
@Slf4j
@Service
public class RateLimiter {

  private static final long CAPACITY = 10; // Max burst
  private static final long REFILL_RATE_MS = 1000; // 1 token per second

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  public boolean tryConsume(String key) {
    return buckets.computeIfAbsent(key, k -> new Bucket(CAPACITY)).tryConsume();
  }

  private static class Bucket {
    private final long capacity;
    private final AtomicLong tokens;
    private long lastRefillTimestamp;

    public Bucket(long capacity) {
      this.capacity = capacity;
      this.tokens = new AtomicLong(capacity);
      this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
      refill();
      if (tokens.get() > 0) {
        tokens.decrementAndGet();
        return true;
      }
      return false;
    }

    private void refill() {
      long now = System.currentTimeMillis();
      long duration = now - lastRefillTimestamp;
      if (duration >= REFILL_RATE_MS) {
        long refillAmount = duration / REFILL_RATE_MS;
        if (refillAmount > 0) {
          long newVal = Math.min(capacity, tokens.get() + refillAmount);
          tokens.set(newVal);
          lastRefillTimestamp = now;
        }
      }
    }
  }
}
