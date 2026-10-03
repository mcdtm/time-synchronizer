package me.kvdpxne.ts.config;

import java.util.Locale;

/**
 * Which Bukkit API is used when applying time to a world while day-sync is disabled.
 */
public enum TimeApplyMode {

  /**
   * {@code World#setTime}. Preserves world age and moon phases.
   */
  SET_TIME,

  /**
   * {@code World#setFullTime} computed from the world's current day.
   * Effectively equivalent to {@link #SET_TIME}, but goes through {@code setFullTime}.
   */
  SET_FULL_TIME;

  /**
   * Parses a case-insensitive, trimmed string into a {@link TimeApplyMode}.
   *
   * @throws IllegalArgumentException when the value does not match any mode
   */
  public static TimeApplyMode fromString(final String raw) {
    if (null == raw) {
      throw new NullPointerException("raw");
    }
    try {
      return valueOf(raw.trim().toUpperCase(Locale.ROOT));
    } catch (final IllegalArgumentException cause) {
      throw new IllegalArgumentException("Unknown time-apply-mode: " + raw, cause);
    }
  }
}