package me.kvdpxne.ts.config;

import java.util.Locale;

/**
 * How the in-game day counter is synchronized with the real-world calendar.
 */
public enum DaySyncMode {

  /** Minecraft manages day counter; only time-of-day is synced. */
  DISABLED,

  /** Day counter matches real calendar; applied to Overworld worlds only. */
  OVERWORLD,

  /** Day counter matches real calendar; applied to every world. */
  ALL_WORLDS;

  /**
   * Parses a case-insensitive, trimmed string into a {@link DaySyncMode}.
   *
   * @throws IllegalArgumentException when the value does not match any mode
   */
  public static DaySyncMode fromString(final String raw) {
    if (null == raw) {
      throw new NullPointerException("raw");
    }
    try {
      return valueOf(raw.trim().toUpperCase(Locale.ROOT));
    } catch (final IllegalArgumentException cause) {
      throw new IllegalArgumentException("Unknown day-sync-mode: " + raw, cause);
    }
  }
}