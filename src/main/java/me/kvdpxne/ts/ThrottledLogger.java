package me.kvdpxne.ts;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Emits at most one log entry per {@code key} within a configurable window.
 * <p>
 * Used to avoid spamming the console when a condition (time jump, gamerule reset)
 * occurs on every scheduler tick.
 * <p>
 * <b>Not thread-safe.</b> Must be used from the main server thread. Entries
 * are removed explicitly via {@link #forget(String)}; otherwise the map grows
 * with the number of distinct keys, so remember to clean up on world unload.
 */
public final class ThrottledLogger {

  private final Logger logger;
  private final long throttleMillis;
  private final Map<String, Long> lastLoggedAt = new HashMap<>();

  public ThrottledLogger(final Logger logger, final long throttleMillis) {
    this.logger = Objects.requireNonNull(logger, "logger");
    if (0L > throttleMillis) {
      throw new IllegalArgumentException("throttleMillis must be non-negative");
    }
    this.throttleMillis = throttleMillis;
  }

  /**
   * Logs a warning for {@code key} at most once per throttle window.
   *
   * @param key      unique identifier (e.g. world name)
   * @param message  message supplier evaluated only when the entry is emitted
   */
  public void warn(final String key, final Supplier<String> message) {
    final long now = System.currentTimeMillis();
    final var last = this.lastLoggedAt.get(key);
    if (null != last && now - last < this.throttleMillis) {
      return;
    }
    this.lastLoggedAt.put(key, now);
    this.logger.warning(message);
  }

  /** Removes the throttle entry for {@code key}, e.g. on world unload. */
  public void forget(final String key) {
    this.lastLoggedAt.remove(key);
  }

  public void clear() {
    this.lastLoggedAt.clear();
  }
}