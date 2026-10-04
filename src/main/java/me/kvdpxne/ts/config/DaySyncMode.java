package me.kvdpxne.ts.config;

import me.kvdpxne.ts.util.parser.EnumParser;

import java.util.Optional;

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
   * @return the parsed mode, or {@link Optional#empty()} when the input is unknown or null
   */
  public static Optional<DaySyncMode> tryFromString(final String raw) {
    return EnumParser.tryParse(DaySyncMode.class, raw);
  }
}